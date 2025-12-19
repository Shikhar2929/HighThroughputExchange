package HighThroughPutExchange.API;

import HighThroughPutExchange.API.api_objects.Operations.*;
import HighThroughPutExchange.API.api_objects.requests.*;
import HighThroughPutExchange.API.api_objects.responses.*;
import HighThroughPutExchange.API.authentication.BotAuthenticator;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import HighThroughPutExchange.API.authentication.RateLimiter;
import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.API.database_objects.User;
import HighThroughPutExchange.Common.Message;
import HighThroughPutExchange.Common.TaskFuture;
import HighThroughPutExchange.Common.TaskQueue;
import HighThroughPutExchange.Database.localdb.LocalDBTable;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import HighThroughPutExchange.MatchingEngine.Order;
import HighThroughPutExchange.MatchingEngine.Side;
import HighThroughPutExchange.MatchingEngine.Status;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;

/*
todo
    make all task queue tasks into completable futures
    fix exception handling of database classes
    make all DBs threadsafe
        Atomicity - not guaranteed, but expose backing and mutex
        Consistency - guaranteed
        Isolation - guaranteed
        Durability - guaranteed
 */

@SpringBootApplication
@RestController
@EnableScheduling
public class ServerApplication {
    private State state;
    private LocalDBTable<User> users;
    private LocalDBTable<User> bots;
    private LocalDBTable<Session> botSessions;
    private LocalDBTable<Session> sessions;
    private PrivatePageAuthenticator privatePageAuthenticator;
    private BotAuthenticator botAuthenticator;
    private RateLimiter rateLimiter;

    private static final int MAX_OPERATIONS = 20;
    private MatchingEngine matchingEngine;

    public ServerApplication(MatchingEngine matchingEngine, RateLimiter rateLimiter, @Qualifier("usersTable") LocalDBTable<User> users,
            @Qualifier("botsTable") LocalDBTable<User> bots, @Qualifier("sessionsTable") LocalDBTable<Session> sessions,
            @Qualifier("botSessionsTable") LocalDBTable<Session> botSessions) {
        this.matchingEngine = matchingEngine;
        this.rateLimiter = rateLimiter;
        this.users = users;
        this.bots = bots;
        this.sessions = sessions;
        this.botSessions = botSessions;
        state = State.STOP;
        Iterable<String> userKeys = this.users.getAllKeys();
        for (String user : userKeys) {
            matchingEngine.initializeUser(user);
        }
        Iterable<String> botKeys = this.bots.getAllKeys();
        for (String bot : botKeys) {
            matchingEngine.initializeBot(bot);
        }

        PrivatePageAuthenticator.buildInstance(this.sessions);
        privatePageAuthenticator = PrivatePageAuthenticator.getInstance();
        BotAuthenticator.buildInstance(this.botSessions);
        botAuthenticator = BotAuthenticator.getInstance();
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    public State getState() {
        return state;
    }

    public void setStateInternal(State newState) {
        this.state = newState;
    }

    /*
     * HTTP Status Codes: - OK: success - UNAUTHORIZED: failed authentication -
     * TOO_MANY_REQUESTS: rate limited - BAD_REQUEST: bad input in HTTP request form
     * - LOCKED: if state is mismatched
     */

    // -------------------- public pages --------------------
    /*
     * @CrossOrigin(origins = "*")
     *
     * @GetMapping("/") public ResponseEntity<String> home() { return new
     * ResponseEntity<>("Welcome to GT Trading Club's High Throughput Exchange!",
     * HttpStatus.OK); }
     */

    /*
     * @PostMapping("/test") public CompletableFuture<String> test(@RequestBody
     * PrivatePageRequest form) { if (form.getUsername().equals("test")) {
     * System.out.println("test is the username"); return
     * CompletableFuture.completedFuture("done sir"); } return
     * CompletableFuture.supplyAsync(() -> { System.out.println(form.getUsername());
     * System.out.println(form.getSessionToken()); try { Thread.sleep(10000); }
     * catch (InterruptedException e) { throw new RuntimeException(e); } return
     * "done"; }); }
     */

    @CrossOrigin(origins = "*")
    @GetMapping("/get_state")
    public ResponseEntity<String> state() {
        return new ResponseEntity<>(String.format("{\"state\": %d}", state.ordinal()), HttpStatus.OK);
    }

    // -------------------- private pages --------------------

    @CrossOrigin(origins = "*")
    @PostMapping("/batch")
    public ResponseEntity<BatchResponse> processBatch(@Valid @RequestBody BatchRequest form) {
        if (!botAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new BatchResponse(Message.AUTHENTICATION_FAILED.toString(), null), HttpStatus.UNAUTHORIZED);
        }
        if (state == State.STOP) {
            return new ResponseEntity<>(new BatchResponse(Message.TRADE_LOCKED.toString(), null), HttpStatus.LOCKED);
        }

        if (form.getOperations().size() > MAX_OPERATIONS) {
            return new ResponseEntity<>(new BatchResponse("EXCEEDED_OPERATION_LIMIT", null), HttpStatus.BAD_REQUEST);
        }
        List<Runnable> temporaryTaskQueue = new ArrayList<>();
        List<OperationResponse> responses = new ArrayList<>();
        List<TaskFuture<String>> futures = new ArrayList<>();
        boolean success = true;
        for (Operation operation : form.getOperations()) {
            TaskFuture<String> future = new TaskFuture<>();
            futures.add(future);
            responses.add(new OperationResponse(operation.getType(), null));
            // System.out.println(operation.toString());
            switch (operation.getType()) {
                case "limit_order" :
                    LimitOrderOperation limitOrderOperation = (LimitOrderOperation) operation;
                    temporaryTaskQueue.add(() -> {
                        Order order = new Order(form.getUsername(), limitOrderOperation.getTicker(), limitOrderOperation.getPrice(),
                                limitOrderOperation.getVolume(), limitOrderOperation.getBid() ? Side.BID : Side.ASK, Status.ACTIVE);
                        // System.out.println("Batch - Adding Limit Order");
                        if (limitOrderOperation.getBid())
                            matchingEngine.bidLimitOrder(form.getUsername(), order, future);
                        else
                            matchingEngine.askLimitOrder(form.getUsername(), order, future);
                        future.markAsComplete();
                    });
                    break;
                case "market_order" :
                    MarketOrderOperation marketOrderOperation = (MarketOrderOperation) operation;
                    temporaryTaskQueue.add(() -> {
                        // System.out.println("Batch - Adding Market Order");
                        if (marketOrderOperation.getBid()) {
                            matchingEngine.bidMarketOrder(form.getUsername(), marketOrderOperation.getTicker(), marketOrderOperation.getVolume(),
                                    future);
                        } else
                            matchingEngine.askMarketOrder(form.getUsername(), marketOrderOperation.getTicker(), marketOrderOperation.getVolume(),
                                    future);
                        future.markAsComplete();
                    });
                    break;
                case "remove" :
                    RemoveOperation removeOperation = (RemoveOperation) operation;
                    temporaryTaskQueue.add(() -> {
                        // System.out.println("Batch - Remove Processing");
                        matchingEngine.removeOrder(form.getUsername(), removeOperation.getOrderId(), future);
                        future.markAsComplete();
                    });
                    break;
                case "remove_all" :
                    temporaryTaskQueue.add(() -> {
                        // System.out.println("Batch - Remove All");
                        matchingEngine.removeAll(form.getUsername(), future);
                        future.markAsComplete();
                    });
                    break;
                default :
                    // todo mark future
                    success = false;
                    break;
            }
        }
        if (!success) {
            return new ResponseEntity<>(new BatchResponse("UNKNOWN OPERATION", null), HttpStatus.BAD_REQUEST);
        }
        for (Runnable task : temporaryTaskQueue) {
            TaskQueue.addTask(task);
        }
        // System.out.println("Batch Tasks Added to the Queue Successfully!");
        for (int i = 0; i < futures.size(); i++) {
            TaskFuture<String> future = futures.get(i);
            future.waitForCompletion();
            // System.out.printf("Batch %d Message: %s\n", i, message);
            OperationResponse response = responses.get(i);
            response.setMessage(future.getData());
        }
        // System.out.println("All Messages Received From Batch Tasks, Returning!");
        return new ResponseEntity<>(new BatchResponse("SUCCESS", responses), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/get_details")
    public ResponseEntity<GetDetailsResponse> getDetails(@Valid @RequestBody PrivatePageRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new GetDetailsResponse(Message.AUTHENTICATION_FAILED.toString(), ""), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimiter.processRequest(form)) {
            return new ResponseEntity<>(new GetDetailsResponse(Message.RATE_LIMITED.toString(), ""), HttpStatus.TOO_MANY_REQUESTS);
        }
        return new ResponseEntity<>(new GetDetailsResponse(Message.SUCCESS.toString(), matchingEngine.getUserDetails(form.getUsername())),
                HttpStatus.OK);
    }
}
