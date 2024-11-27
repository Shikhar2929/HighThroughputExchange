package HighThroughPutExchange.API.api_objects.responses;

public class AddUserResponse {
    private boolean auth;
    private boolean success;
    private String message;


    public AddUserResponse(boolean auth, boolean success, String message) {
        this.auth = auth;
        this.success = success;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
