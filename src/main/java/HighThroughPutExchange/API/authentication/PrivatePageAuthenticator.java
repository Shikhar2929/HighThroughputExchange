package HighThroughPutExchange.API.authentication;

import HighThroughPutExchange.API.api_objects.requests.BasePrivateRequest;
import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.Database.localdb.LocalDBTable;

public class PrivatePageAuthenticator {
    private static PrivatePageAuthenticator instance;
    private final LocalDBTable<Session> sessions;

    private PrivatePageAuthenticator(LocalDBTable<Session> sessions) {
        this.sessions = sessions;
    }

    public static void buildInstance(LocalDBTable<Session> sessions) {
        instance = new PrivatePageAuthenticator(sessions);
    }

    public static PrivatePageAuthenticator getInstance() {
        return instance;
    }

    public boolean authenticate(BasePrivateRequest req) {
        // if username not found
        if (!sessions.containsItem(req.getUsername())) {
            return false;
        }

        Session s = sessions.getItem(req.getUsername());
        // if username and api key mismatch
        if (!s.getSessionToken().equals(req.getSessionToken()) && !s.getSessionToken2().equals(req.getSessionToken())) {
            return false;
        }

        return true;
    }
}
