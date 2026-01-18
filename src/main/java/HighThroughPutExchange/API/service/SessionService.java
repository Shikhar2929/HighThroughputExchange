package HighThroughPutExchange.API.service;

import HighThroughPutExchange.API.api_objects.responses.BuildupResponse;
import HighThroughPutExchange.API.api_objects.responses.TeardownResponse;
import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.API.database_objects.User;
import HighThroughPutExchange.Common.Message;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.Database.localdb.LocalDBTable;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final LocalDBTable<User> users;
    private final LocalDBTable<User> bots;
    private final LocalDBTable<Session> sessions;
    private final LocalDBTable<Session> botSessions;
    private final MatchingEngine matchingEngine;

    public SessionService(
            @Qualifier("usersTable") LocalDBTable<User> users,
            @Qualifier("botsTable") LocalDBTable<User> bots,
            @Qualifier("sessionsTable") LocalDBTable<Session> sessions,
            @Qualifier("botSessionsTable") LocalDBTable<Session> botSessions,
            MatchingEngine matchingEngine) {
        this.users = users;
        this.bots = bots;
        this.sessions = sessions;
        this.botSessions = botSessions;
        this.matchingEngine = matchingEngine;
    }

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

    public BuildupResponse buildup(String username, String apiKey) {
        if (!users.containsItem(username)) {
            return new BuildupResponse(Message.AUTHENTICATION_FAILED.toString(), "", "");
        }

        User u = users.getItem(username);
        if (!u.getApiKey().equals(apiKey) && !u.getApiKey2().equals(apiKey)) {
            return new BuildupResponse(Message.AUTHENTICATION_FAILED.toString(), "", "");
        }

        String sessionToken = generateKey();
        if (sessions.containsItem(username)) {
            if (u.getApiKey().equals(apiKey)) {
                sessions.getItem(username).setSessionToken(sessionToken);
            } else {
                sessions.getItem(username).setSessionToken2(sessionToken);
            }
        } else {
            Session s = new Session(sessionToken, u.getUsername());
            try {
                sessions.putItem(s);
            } catch (AlreadyExistsException e) {
                throw new RuntimeException(e);
            }
        }
        return new BuildupResponse(
                Message.SUCCESS.toString(), sessionToken, matchingEngine.serializeOrderBooks());
    }

    public BuildupResponse botBuildup(String username, String apiKey) {
        if (!bots.containsItem(username)) {
            return new BuildupResponse(Message.AUTHENTICATION_FAILED.toString(), "", "");
        }

        User u = bots.getItem(username);
        if (!u.getApiKey().equals(apiKey)) {
            return new BuildupResponse(Message.AUTHENTICATION_FAILED.toString(), "", "");
        }

        Session s = new Session(generateKey(), u.getUsername());
        if (botSessions.containsItem(s.getUsername())) {
            botSessions.deleteItem(s.getUsername());
        }
        try {
            botSessions.putItem(s);
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        if (sessions.containsItem(s.getUsername())) {
            sessions.deleteItem(s.getUsername());
        }
        try {
            sessions.putItem(s);
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        return new BuildupResponse(
                Message.SUCCESS.toString(),
                s.getSessionToken(),
                matchingEngine.serializeOrderBooks());
    }

    public TeardownResponse teardown(String username) {
        if (sessions.containsItem(username)) {
            sessions.deleteItem(username);
        }
        return new TeardownResponse(Message.SUCCESS.toString());
    }
}
