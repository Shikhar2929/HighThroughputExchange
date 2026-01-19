package hte.api.service;

import hte.api.dtos.responses.BuildupResponse;
import hte.api.dtos.responses.TeardownResponse;
import hte.api.entities.Session;
import hte.api.entities.User;
import hte.api.repository.BotSessionsRepository;
import hte.api.repository.BotsRepository;
import hte.api.repository.SessionsRepository;
import hte.api.repository.UsersRepository;
import hte.common.Message;
import hte.database.exceptions.AlreadyExistsException;
import hte.matchingengine.MatchingEngine;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final UsersRepository users;
    private final BotsRepository bots;
    private final SessionsRepository sessions;
    private final BotSessionsRepository botSessions;
    private final MatchingEngine matchingEngine;

    public SessionService(
            UsersRepository users,
            BotsRepository bots,
            SessionsRepository sessions,
            BotSessionsRepository botSessions,
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
        if (!users.exists(username)) {
            return new BuildupResponse(Message.AUTHENTICATION_FAILED.toString(), "", "");
        }

        User u = users.get(username);
        if (!u.getApiKey().equals(apiKey) && !u.getApiKey2().equals(apiKey)) {
            return new BuildupResponse(Message.AUTHENTICATION_FAILED.toString(), "", "");
        }

        String sessionToken = generateKey();
        if (sessions.exists(username)) {
            if (u.getApiKey().equals(apiKey)) {
                sessions.get(username).setSessionToken(sessionToken);
            } else {
                sessions.get(username).setSessionToken2(sessionToken);
            }
        } else {
            Session s = new Session(sessionToken, u.getUsername());
            try {
                sessions.add(s);
            } catch (AlreadyExistsException e) {
                throw new RuntimeException(e);
            }
        }
        return new BuildupResponse(
                Message.SUCCESS.toString(), sessionToken, matchingEngine.serializeOrderBooks());
    }

    public BuildupResponse botBuildup(String username, String apiKey) {
        if (!bots.exists(username)) {
            return new BuildupResponse(Message.AUTHENTICATION_FAILED.toString(), "", "");
        }

        User u = bots.get(username);
        if (!u.getApiKey().equals(apiKey)) {
            return new BuildupResponse(Message.AUTHENTICATION_FAILED.toString(), "", "");
        }

        Session s = new Session(generateKey(), u.getUsername());
        if (botSessions.exists(s.getUsername())) {
            botSessions.delete(s.getUsername());
        }
        try {
            botSessions.add(s);
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        if (sessions.exists(s.getUsername())) {
            sessions.delete(s.getUsername());
        }
        try {
            sessions.add(s);
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        return new BuildupResponse(
                Message.SUCCESS.toString(),
                s.getSessionToken(),
                matchingEngine.serializeOrderBooks());
    }

    public TeardownResponse teardown(String username) {
        if (sessions.exists(username)) {
            sessions.delete(username);
        }
        return new TeardownResponse(Message.SUCCESS.toString());
    }
}
