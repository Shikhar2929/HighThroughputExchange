package HighThroughPutExchange.API.authentication;

import HighThroughPutExchange.API.api_objects.requests.BasePrivateRequest;
import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.API.repository.BotSessionsRepository;
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
