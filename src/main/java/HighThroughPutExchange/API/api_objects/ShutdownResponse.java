package HighThroughPutExchange.API.api_objects;

public class ShutdownResponse {
    private boolean success;
    private boolean auth;


    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean getAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public ShutdownResponse(boolean success, boolean auth) {
        this.success = success;
        this.auth = auth;
    }
}
