package HighThroughPutExchange.API.service;

import HighThroughPutExchange.API.ServerApplication;
import HighThroughPutExchange.API.State;
import HighThroughPutExchange.API.database_objects.User;
import HighThroughPutExchange.Common.TaskFuture;
import HighThroughPutExchange.Common.TaskQueue;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.Database.localdb.LocalDBClient;
import HighThroughPutExchange.Database.localdb.LocalDBTable;
import HighThroughPutExchange.MatchingEngine.LeaderboardEntry;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private static final int KEY_LENGTH = 16;

    private final LocalDBClient dbClient;
    private final LocalDBTable<User> users;
    private final MatchingEngine matchingEngine;
    private final LocalDBTable<User> bots;
    private final ServerApplication app;

    public AdminService(LocalDBClient dbClient, @Qualifier("usersTable") LocalDBTable<User> users, @Qualifier("botsTable") LocalDBTable<User> bots,
            MatchingEngine matchingEngine, ServerApplication app) {
        this.dbClient = dbClient;
        this.users = users;
        this.bots = bots;
        this.matchingEngine = matchingEngine;
        this.app = app;
    }

    public boolean usernameExists(String username) {
        return users.containsItem(username);
    }

    public User addUser(String username, String name, String email) {
        String apiKey1 = generateKey();
        String apiKey2 = generateKey();
        User user = new User(username, name, apiKey1, apiKey2, email);
        try {
            users.putItem(user);
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        TaskQueue.addTask(() -> matchingEngine.initializeUser(username));
        return user;
    }

    public String addBot(String username) {
        String key = generateKey();
        try {
            users.putItem(new User(username, "", key, ""));
            bots.putItem(new User(username, "", key, ""));
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        TaskQueue.addTask(() -> matchingEngine.initializeBot(username));
        return key;
    }

    public void shutdown() {
        try {
            dbClient.closeClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<LeaderboardEntry> getLeaderboard() {
        TaskFuture<ArrayList<LeaderboardEntry>> future = new TaskFuture<>();
        TaskQueue.addTask(() -> matchingEngine.getLeaderboard(future));
        future.waitForCompletion();
        return future.getData();
    }

    public String setPrice(Map<String, Integer> prices) {
        TaskFuture<String> future = new TaskFuture<>();
        TaskQueue.addTask(() -> {
            matchingEngine.setPriceClearOrderBook(prices, future);
            future.markAsComplete();
        });
        future.waitForCompletion();
        return future.getData();
    }

    public int applyState(int targetState) {
        State newState = State.values()[targetState];
        app.setStateInternal(newState);
        return newState.ordinal();
    }

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
}
