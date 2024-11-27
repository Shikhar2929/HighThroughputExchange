package HighThroughPutExchange.API.database_objects;

import HighThroughPutExchange.Database.entry.DBEntry;

public class Session extends DBEntry {

    private String sessionToken;
    private String username;

    public Session(String sessionToken, String username) {
        this.sessionToken = sessionToken;
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
}
