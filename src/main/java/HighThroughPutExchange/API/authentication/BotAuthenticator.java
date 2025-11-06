package HighThroughPutExchange.API.authentication;

import HighThroughPutExchange.API.api_objects.requests.BasePrivateRequest;
import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.Database.localdb.LocalDBTable;

public class BotAuthenticator {
    private static BotAuthenticator instance;
    private final LocalDBTable<Session> sessions;

    private BotAuthenticator(LocalDBTable<Session> sessions) {
        this.sessions = sessions;
    }

    public static void buildInstance(LocalDBTable<Session> sessions) {
        instance = new BotAuthenticator(sessions);
    }

    public static BotAuthenticator getInstance() {
        return instance;
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
