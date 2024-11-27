package HighThroughPutExchange.API.api_objects.requests;

public class BasePrivateRequest {
    protected String username;
    protected String sessionToken;
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public BasePrivateRequest(String username, String sessionToken) {
        this.username = username;
        this.sessionToken = sessionToken;
    }
}
