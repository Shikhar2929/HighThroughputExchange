package HighThroughPutExchange.API.database_objects;

import HighThroughPutExchange.Database.entry.DBEntry;

public class Session extends DBEntry {

    private String sessionToken;
    private String sessionToken2;
    private String username;

    public Session() {
    }

    public Session(String sessionToken, String username) {
        this(sessionToken, sessionToken, username);
    }

    public Session(String sessionToken, String sessionToken2, String username) {
        this.sessionToken = sessionToken;
        this.sessionToken2 = sessionToken2;
        this.username = username;
    }

    @Override
    public String hashOut() {
        return username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionToken2() {
        return sessionToken2;
    }

    public void setSessionToken2(String sessionToken2) {
        this.sessionToken2 = sessionToken2;
    }
}
