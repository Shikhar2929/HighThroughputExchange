package HighThroughPutExchange.API;

import HighThroughPutExchange.API.api_objects.requests.*;
import HighThroughPutExchange.API.api_objects.responses.*;
import HighThroughPutExchange.API.authentication.AdminPageAuthenticator;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.API.database_objects.User;
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.validation.Valid;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.HashMap;
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


    private LocalDBClient dbClient;
    private LocalDBTable<User> users;
    private LocalDBTable<Session> sessions;
    private PrivatePageAuthenticator privatePageAuthenticator;
    private AdminPageAuthenticator adminPageAuthenticator;
    private MatchingEngine matchingEngine;
    private static final int KEY_LENGTH = 16;

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
        HashMap<String, Class<? extends DBEntry>> mapping = new HashMap<>();
        matchingEngine = new MatchingEngine();
        matchingEngine.initializeAllTickers();
        mapping.put("users", User.class);
        mapping.put("sessions", Session.class);
        dbClient = new LocalDBClient("data.json", mapping);
        try {
            users = dbClient.getTable("users");
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

        PrivatePageAuthenticator.buildInstance(sessions);
        adminPageAuthenticator = AdminPageAuthenticator.getInstance();
        privatePageAuthenticator = PrivatePageAuthenticator.getInstance();

    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    // public pages

    @GetMapping("/")
    public String home() {
        return "Hello, World! This is a public page.";
    }

    // admin pages

    @PostMapping("/add_user")
    public AddUserResponse addUser(@Valid @RequestBody AddUserRequest form) {
        if (!adminPageAuthenticator.authenticate(form)) {
            return new AddUserResponse(false, false, "incorrect username or password", "");
        }

        // no duplicate usernames
        if (users.containsItem(form.getUsername())) {
            return new AddUserResponse(true, false, "username already exists", "");
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
        return new AddUserResponse(true, true, "user successfully created", users.getItem(form.getUsername()).getApiKey());
    }

    @PostMapping("/admin_page")
    public AdminDashboardResponse adminPage(@Valid @RequestBody AdminDashboardRequest form) {
        if (!adminPageAuthenticator.authenticate(form)) {
            return new AdminDashboardResponse(false, false, "failed authentication");
        }

        return new AdminDashboardResponse(true, false, "this is the admin dashboard");
    }

    @PostMapping("/shutdown")
    public ShutdownResponse shutdown(@Valid @RequestBody ShutdownRequest form) {
        if (!adminPageAuthenticator.authenticate(form)) {
            return new ShutdownResponse(false, false);
        }

        try {
            dbClient.closeClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ShutdownResponse(true, true);
    }

    // private pages

    @PostMapping("/buildup")
    public BuildupResponse buildup(@Valid @RequestBody BuildupRequest form) {
        /*
        Note that BuildupRequest does not inherit from BasePrivateRequest because it uses the API key, as oppsed to session token.
         */
        // if username not found
        if (!users.containsItem(form.getUsername())) {
            return new BuildupResponse(false, false, "", "");
        }

        User u = users.getItem(form.getUsername());
        // if username and api key mismatch
        if (!u.getApiKey().equals(form.getApiKey())) {
            return new BuildupResponse(false, false, "", "");
        }

        Session s = new Session(generateKey(), u.getUsername());
        try {
            sessions.putItem(s);
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        return new BuildupResponse(true, true, s.getSessionToken(), matchingEngine.serializeOrderBooks());
    }

    @PostMapping("/teardown")
    public TeardownResponse teardown(@Valid @RequestBody TeardownRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new TeardownResponse(false, false);
        }

        sessions.deleteItem(form.getUsername());

        return new TeardownResponse(true, true);
    }

    @PostMapping("/privatePage")
    public PrivatePageResponse privatePage(@Valid @RequestBody PrivatePageRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new PrivatePageResponse(false, false, "");
        }

        return new PrivatePageResponse(true, true, "this is a private page");
    }

    @PostMapping("/limit_order")
    public LimitOrderResponse limitOrder(@Valid @RequestBody LimitOrderRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new LimitOrderResponse(false, false);
        }
        TaskQueue.addTask(() -> {
            Order order = new Order(form.getUsername(), form.getTicker(), form.getPrice(), form.getVolume(),
                    form.getBid() ? Side.BID : Side.ASK, Status.ACTIVE);
            System.out.println("Adding: " + order.toString());
            if (form.getBid())
                matchingEngine.bidLimitOrder(form.getUsername(), order);
            else
                matchingEngine.askLimitOrder(form.getUsername(), order);
        });
        return new LimitOrderResponse(true, true);
    }
    @PostMapping("/remove_all")
    public RemoveAllResponse removeAll(@Valid @RequestBody BasePrivateRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new RemoveAllResponse(false, false);
        }
        if (form.getUsername() == null)
            return new RemoveAllResponse(true, false);
        System.out.println("Script username ain't the issue here: " + form.getUsername());
        TaskQueue.addTask(() -> {
            matchingEngine.removeAll(form.getUsername());
        });
        return new RemoveAllResponse(true, true);
    }
}
