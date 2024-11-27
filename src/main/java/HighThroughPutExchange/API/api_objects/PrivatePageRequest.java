package HighThroughPutExchange.API.api_objects;

public class PrivatePageRequest {
    private String username;
    private String sessionToken;

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

    public PrivatePageRequest(String username, String sessionToken) {
        this.username = username;
        this.sessionToken = sessionToken;
    }
}
