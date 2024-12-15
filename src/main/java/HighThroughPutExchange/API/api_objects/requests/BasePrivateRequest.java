package HighThroughPutExchange.API.api_objects.requests;
import jakarta.validation.constraints.NotNull;

public class BasePrivateRequest {
    @NotNull
    protected String username;
    @NotNull
    protected String sessionToken;
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        //this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.username = username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {

        //this.sessionToken = Objects.requireNonNull(sessionToken, "Session token cannot be null");
        this.sessionToken = sessionToken;
    }

    public BasePrivateRequest(String username, String sessionToken) {
        //this.username = Objects.requireNonNull(username, "Username cannot be null");
        //this.sessionToken = Objects.requireNonNull(sessionToken, "Session token cannot be null");
        this.username = username;
        this.sessionToken = sessionToken;
    }
}
