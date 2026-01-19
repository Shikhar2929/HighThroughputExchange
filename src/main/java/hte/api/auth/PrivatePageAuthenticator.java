package hte.api.auth;

import hte.api.dtos.requests.BasePrivateRequest;
import hte.api.entities.Session;
import hte.api.repository.SessionsRepository;
import org.springframework.stereotype.Component;

@Component
public class PrivatePageAuthenticator {
    private final SessionsRepository sessions;

    public PrivatePageAuthenticator(SessionsRepository sessions) {
        this.sessions = sessions;
    }

    public boolean authenticate(BasePrivateRequest req) {
        // if username not found
        if (!sessions.exists(req.getUsername())) {
            return false;
        }

        Session s = sessions.get(req.getUsername());
        // if username and api key mismatch
        if (!s.getSessionToken().equals(req.getSessionToken())
                && !s.getSessionToken2().equals(req.getSessionToken())) {
            return false;
        }

        return true;
    }
}
