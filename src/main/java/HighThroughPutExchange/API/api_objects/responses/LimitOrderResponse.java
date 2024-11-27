package HighThroughPutExchange.API.api_objects.responses;

public class LimitOrderResponse {
    private boolean auth;
    private boolean success;

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

    public LimitOrderResponse(boolean auth, boolean success) {
        this.auth = auth;
        this.success = success;
    }
}
