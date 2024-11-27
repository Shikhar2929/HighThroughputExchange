package HighThroughPutExchange.API.api_objects;

public class TeardownResponse {
    private boolean auth;
    private boolean success;


    public TeardownResponse(boolean auth, boolean success) {
        this.auth = auth;
        this.success = success;
    }

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
}
