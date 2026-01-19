package hte.api.service;

import hte.api.ServerApplication;
import hte.api.State;
import hte.api.entities.User;
import hte.api.repository.BotsRepository;
import hte.api.repository.DbLifecycleRepository;
import hte.api.repository.UsersRepository;
import hte.common.TaskFuture;
import hte.common.TaskQueue;
import hte.database.exceptions.AlreadyExistsException;
import hte.matchingengine.LeaderboardEntry;
import hte.matchingengine.MatchingEngine;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private static final int KEY_LENGTH = 16;

    private final DbLifecycleRepository dbLifecycle;
    private final UsersRepository users;
    private final MatchingEngine matchingEngine;
    private final BotsRepository bots;
    private final ServerApplication app;

    public AdminService(
            DbLifecycleRepository dbLifecycle,
            UsersRepository users,
            BotsRepository bots,
            MatchingEngine matchingEngine,
            ServerApplication app) {
        this.dbLifecycle = dbLifecycle;
        this.users = users;
        this.bots = bots;
        this.matchingEngine = matchingEngine;
        this.app = app;
    }

    public boolean usernameExists(String username) {
        return users.exists(username);
    }

    public User addUser(String username, String name, String email) {
        String apiKey1 = generateKey();
        String apiKey2 = generateKey();
        User user = new User(username, name, apiKey1, apiKey2, email);
        try {
            users.add(user);
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        TaskQueue.addTask(() -> matchingEngine.initializeUser(username));
        return user;
    }

    public String addBot(String username) {
        String key = generateKey();
        try {
            users.add(new User(username, "", key, ""));
            bots.add(new User(username, "", key, ""));
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        TaskQueue.addTask(() -> matchingEngine.initializeBot(username));
        return key;
    }

    public void shutdown() {
        try {
            dbLifecycle.close();
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
        TaskQueue.addTask(
                () -> {
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
