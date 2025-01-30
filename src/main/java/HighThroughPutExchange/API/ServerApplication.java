package HighThroughPutExchange.API;

import HighThroughPutExchange.API.api_objects.requests.*;
import HighThroughPutExchange.API.api_objects.responses.*;
import HighThroughPutExchange.API.authentication.AdminPageAuthenticator;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import HighThroughPutExchange.API.authentication.RateLimiter;
import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.API.database_objects.User;
import HighThroughPutExchange.Common.MatchingEngineSingleton;
import HighThroughPutExchange.Common.TaskFuture;
import HighThroughPutExchange.Common.TaskQueue;
import HighThroughPutExchange.Database.entry.DBEntry;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.Database.exceptions.NotFoundException;
import HighThroughPutExchange.Database.localdb.LocalDBClient;
import HighThroughPutExchange.Database.localdb.LocalDBTable;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import HighThroughPutExchange.MatchingEngine.Order;
import HighThroughPutExchange.MatchingEngine.Side;
import HighThroughPutExchange.MatchingEngine.Status;
import HighThroughPutExchange.MatchingEngine.LeaderboardEntry;
import HighThroughPutExchange.Auction.Auction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
/*
todo
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
    private LocalDBClient dbClient;
    private LocalDBTable<User> users;
    private LocalDBTable<Session> sessions;
    private PrivatePageAuthenticator privatePageAuthenticator;
    private AdminPageAuthenticator adminPageAuthenticator;
    private RateLimiter rateLimiter;

    private Auction auction;
    private static final int KEY_LENGTH = 16;
    private MatchingEngine matchingEngine;

    private static char randomChar() {
        return (char) ((int) (Math.random() * 26 + 65));
    }

    private static String generateKey() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < KEY_LENGTH; ++i) {
            output.append(randomChar());
        }
        return output.toString();
    }
    public ServerApplication() {
        state = State.STOP;
        HashMap<String, Class<? extends DBEntry>> mapping = new HashMap<>();
        matchingEngine = MatchingEngineSingleton.getMatchingEngine();
        matchingEngine.initializeAllTickers();
        mapping.put("users", User.class);
        mapping.put("sessions", Session.class);
        dbClient = new LocalDBClient("data.json", mapping);
        try {
            users = dbClient.getTable("users");
            Iterable<String> iterable = users.getAllKeys();
            for (String user : iterable) {
                matchingEngine.initializeUser(user);
            }

        } catch (NotFoundException e) {
            try {
                dbClient.createTable("users");
                users = dbClient.getTable("users");
            } catch (AlreadyExistsException ex) {
                throw new RuntimeException(ex);
            }
        }
        try {
            sessions = dbClient.getTable("sessions");
        } catch (NotFoundException e) {
            try {
                dbClient.createTable("sessions");
                sessions = dbClient.getTable("sessions");
            } catch (AlreadyExistsException ex) {
                throw new RuntimeException(ex);
            }
        }

        // PrivatePageAuthenticator privatePageAuthenticator = new PrivatePageAuthenticator(sessions);
        adminPageAuthenticator = AdminPageAuthenticator.getInstance();
        PrivatePageAuthenticator.buildInstance(sessions);
        privatePageAuthenticator = PrivatePageAuthenticator.getInstance();
        rateLimiter = new RateLimiter();
        auction = new Auction(matchingEngine);
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    /*
    HTTP Status Codes:
        - OK: success
        - UNAUTHORIZED: failed authentication
        - TOO_MANY_REQUESTS: rate limited
        - BAD_REQUEST: bad input in HTTP request form
        - LOCKED: if state is mismatched
     */

    // -------------------- public pages --------------------

    @CrossOrigin(origins = "*")
    @GetMapping("/")
    public ResponseEntity<String> home() {
        return new ResponseEntity<>("Welcome to GT Trading Club's High Throughput Exchange!", HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/get_state")
    public ResponseEntity<String> state() {
        return new ResponseEntity<>(String.format("{\"state\": %d}", state.ordinal()), HttpStatus.OK);
    }

    // -------------------- admin pages --------------------

    @CrossOrigin(origins = "*")
    @PostMapping("/admin_page")
    public ResponseEntity<AdminDashboardResponse> adminPage(@Valid @RequestBody AdminDashboardRequest form) {
        if (!adminPageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new AdminDashboardResponse(false, false, "failed authentication"), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(new AdminDashboardResponse(true, false, "this is the admin dashboard"), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/add_user")
    public ResponseEntity<AddUserResponse> addUser(@Valid @RequestBody AddUserRequest form) {
        if (!adminPageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new AddUserResponse(false, false, "incorrect username or password", ""), HttpStatus.UNAUTHORIZED);
        }

        // no duplicate usernames
        if (users.containsItem(form.getUsername())) {
            return new ResponseEntity<>(new AddUserResponse(true, false, "username already exists", ""), HttpStatus.BAD_REQUEST);
        }
        try {
            users.putItem(new User(form.getUsername(), form.getName(), generateKey(), form.getEmail()));
            TaskQueue.addTask(() -> {
                System.out.println("User initialized" + form.getUsername());
                matchingEngine.initializeUser(form.getUsername());
            });
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(new AddUserResponse(true, true, "user successfully created", users.getItem(form.getUsername()).getApiKey()), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/shutdown")
    public ResponseEntity<ShutdownResponse> shutdown(@Valid @RequestBody ShutdownRequest form) {
        if (!adminPageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new ShutdownResponse(false, false), HttpStatus.UNAUTHORIZED);
        }

        try {
            dbClient.closeClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(new ShutdownResponse(true, true), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponse> leaderboard(@Valid @RequestBody LeaderboardRequest form) {
        if (!adminPageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new LeaderboardResponse(), HttpStatus.UNAUTHORIZED);
        }

        TaskFuture<List<LeaderboardEntry>> future = new TaskFuture<>();
        TaskQueue.addTask(() -> {
            matchingEngine.getLeaderboard(future);
        });

        future.waitForCompletion();

        return new ResponseEntity<>(new LeaderboardResponse(future.getData()), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/set_state")
    public ResponseEntity<SetStateResponse> setState(@Valid @RequestBody SetStateRequest form) {
        if (!adminPageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new SetStateResponse(state.ordinal()), HttpStatus.UNAUTHORIZED);
        }

        if (form.getTargetState() >= State.values().length || form.getTargetState() < 0) {
            return new ResponseEntity<>(new SetStateResponse(state.ordinal()), HttpStatus.BAD_REQUEST);
        }

        state = State.values()[form.getTargetState()];

        return new ResponseEntity<>(new SetStateResponse(state.ordinal()), HttpStatus.OK);
    }

    // -------------------- private pages --------------------

    @CrossOrigin(origins = "*")
    @PostMapping("/buildup")
    public ResponseEntity<BuildupResponse> buildup(@Valid @RequestBody BuildupRequest form) {
        /*
        Note that BuildupRequest does not inherit from BasePrivateRequest because it uses the API key, as oppsed to session token.
         */
        // if username not found
        if (!users.containsItem(form.getUsername())) {
            return new ResponseEntity<>(new BuildupResponse(false, false, "", ""), HttpStatus.UNAUTHORIZED);
        }

        User u = users.getItem(form.getUsername());
        // if username and api key mismatch
        if (!u.getApiKey().equals(form.getApiKey())) {
            return new ResponseEntity<>(new BuildupResponse(false, false, "", ""), HttpStatus.UNAUTHORIZED);
        }

        Session s = new Session(generateKey(), u.getUsername());
        if (sessions.containsItem(s.getUsername())) {
            sessions.deleteItem(s.getUsername());
        }
        try {
            sessions.putItem(s);
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(new BuildupResponse(true, true, s.getSessionToken(), matchingEngine.serializeOrderBooks()), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/teardown")
    public ResponseEntity<TeardownResponse> teardown(@Valid @RequestBody TeardownRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new TeardownResponse(false, false), HttpStatus.UNAUTHORIZED);
        }

        sessions.deleteItem(form.getUsername());

        return new ResponseEntity<>(new TeardownResponse(true, true), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/privatePage")
    public ResponseEntity<PrivatePageResponse> privatePage(@Valid @RequestBody PrivatePageRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new PrivatePageResponse(false, false, ""), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimiter.processRequest(form)) {
            return new ResponseEntity<>(new PrivatePageResponse(true, false, "rate limited"), HttpStatus.TOO_MANY_REQUESTS);
        }

        return new ResponseEntity<>(new PrivatePageResponse(true, true, "this is a private page"), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/limit_order")
    public ResponseEntity<LimitOrderResponse> limitOrder(@Valid @RequestBody LimitOrderRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new LimitOrderResponse(false, false), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimiter.processRequest(form)) {
            return new ResponseEntity<>(new LimitOrderResponse(true, false), HttpStatus.TOO_MANY_REQUESTS);
        }
        if (state != State.TRADE) {
            return new ResponseEntity<>(new LimitOrderResponse(true, false), HttpStatus.LOCKED);
        }
        TaskQueue.addTask(() -> {
            Order order = new Order(form.getUsername(), form.getTicker(), form.getPrice(), form.getVolume(),
                    form.getBid() ? Side.BID : Side.ASK, Status.ACTIVE);
            System.out.println("Adding Limit Order");
            if (form.getBid())
                matchingEngine.bidLimitOrder(form.getUsername(), order);
            else
                matchingEngine.askLimitOrder(form.getUsername(), order);
        });
        return new ResponseEntity<>(new LimitOrderResponse(true, true), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/remove_all")
    public ResponseEntity<RemoveAllResponse> removeAll(@Valid @RequestBody RemoveAllRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(false, false), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimiter.processRequest(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(true, false), HttpStatus.TOO_MANY_REQUESTS);
        }
        if (state != State.TRADE) {
            return new ResponseEntity<>(new RemoveAllResponse(true, false), HttpStatus.LOCKED);
        }
        if (form.getUsername() == null)
            return new ResponseEntity<>(new RemoveAllResponse(true, false), HttpStatus.UNAUTHORIZED);
        TaskQueue.addTask(() -> {
            matchingEngine.removeAll(form.getUsername());
        });
        return new ResponseEntity<>(new RemoveAllResponse(true, true), HttpStatus.OK);
    }
    @CrossOrigin(origins = "*")
    @PostMapping("/remove")
    public ResponseEntity<RemoveAllResponse> remove(@Valid @RequestBody RemoveRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(false, false), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimiter.processRequest(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(true, false), HttpStatus.TOO_MANY_REQUESTS);
        }
        if (state != State.TRADE) {
            return new ResponseEntity<>(new RemoveAllResponse(true, false), HttpStatus.LOCKED);
        }
        if (form.getUsername() == null)
            return new ResponseEntity<>(new RemoveAllResponse(true, false), HttpStatus.UNAUTHORIZED);
        System.out.println("Removing Order");
        System.out.println(form.getOrderID());
        TaskQueue.addTask(() -> {
            matchingEngine.removeOrder(form.getUsername(), form.getOrderID());
        });
        return new ResponseEntity<>(new RemoveAllResponse(true, true), HttpStatus.OK);
    }
    @CrossOrigin(origins = "*")
    @PostMapping("/market_order")
    public ResponseEntity<MarketOrderResponse> marketOrderResponse(@Valid @RequestBody MarketOrderRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new MarketOrderResponse(false, false), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimiter.processRequest(form)) {
            return new ResponseEntity<>(new MarketOrderResponse(true, false), HttpStatus.TOO_MANY_REQUESTS);
        }
        if (state != State.TRADE) {
            return new ResponseEntity<>(new MarketOrderResponse(true, false), HttpStatus.LOCKED);
        }
        TaskQueue.addTask(() -> {
            System.out.println("Adding Market Order: " + form);
            if (form.getBid())
                matchingEngine.bidMarketOrder(form.getUsername(), form.getTicker(), form.getVolume());
            else
                matchingEngine.askMarketOrder(form.getUsername(), form.getTicker(), form.getVolume());
        });
        return new ResponseEntity<>(new MarketOrderResponse(true, true), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/get_details")
    public ResponseEntity<GetDetailsResponse> getDetails(@Valid @RequestBody PrivatePageRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new GetDetailsResponse(false, false, ""), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimiter.processRequest(form)) {
            return new ResponseEntity<>(new GetDetailsResponse(false, false, ""), HttpStatus.TOO_MANY_REQUESTS);
        }
        return new ResponseEntity<>(new GetDetailsResponse(true, true, matchingEngine.getUserDetails(form.getUsername())), HttpStatus.OK);
    }
}
