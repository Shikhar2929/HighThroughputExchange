package HighThroughPutExchange.API.authentication;

import HighThroughPutExchange.API.api_objects.requests.BasePrivateRequest;
import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.Database.localdb.LocalDBTable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BotAuthenticator {
    private final LocalDBTable<Session> sessions;

    public BotAuthenticator(@Qualifier("botSessionsTable") LocalDBTable<Session> sessions) {
        this.sessions = sessions;
    }

    public boolean authenticate(BasePrivateRequest req) {
        // if username not found
        if (!sessions.containsItem(req.getUsername())) {
            return false;
        }

        Session s = sessions.getItem(req.getUsername());
        // if username and api key mismatch
        if (!s.getSessionToken().equals(req.getSessionToken())) {
            return false;
        }
        return true;
    }
}
