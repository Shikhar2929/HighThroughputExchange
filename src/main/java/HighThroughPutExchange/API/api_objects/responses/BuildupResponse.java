package HighThroughPutExchange.API.api_objects.responses;

public class BuildupResponse {
    private boolean auth;
    private boolean success;
    private String sessionToken;
    private String orderBookData;

    public boolean getAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    public String getOrderBookData() {
        return orderBookData;
    }
    public BuildupResponse(boolean auth, boolean success, String sessionToken, String orderBookData) {
        this.auth = auth;
        this.success = success;
        this.sessionToken = sessionToken;
        this.orderBookData = orderBookData;
    }
}
