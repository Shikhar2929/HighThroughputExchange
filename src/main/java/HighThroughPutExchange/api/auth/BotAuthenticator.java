package HighThroughPutExchange.api.auth;

import HighThroughPutExchange.api.dtos.requests.BasePrivateRequest;
import HighThroughPutExchange.api.entities.Session;
import HighThroughPutExchange.api.repository.BotSessionsRepository;
import org.springframework.stereotype.Component;

@Component
public class BotAuthenticator {
    private final BotSessionsRepository sessions;

    public BotAuthenticator(BotSessionsRepository sessions) {
        this.sessions = sessions;
    }

    public boolean authenticate(BasePrivateRequest req) {
        // if username not found
        if (!sessions.exists(req.getUsername())) {
            return false;
        }

        Session s = sessions.get(req.getUsername());
        // if username and api key mismatch
        if (!s.getSessionToken().equals(req.getSessionToken())) {
            return false;
        }
        return true;
    }
}
