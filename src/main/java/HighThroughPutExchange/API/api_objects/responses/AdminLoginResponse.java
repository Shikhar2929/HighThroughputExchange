package HighThroughPutExchange.API.api_objects.responses;

public class AdminLoginResponse {
    private boolean success;
    private String sessionID;

    public AdminLoginResponse(boolean success, String sessionID) {
        this.success = success;
        this.sessionID = sessionID;
    }

    public String getSessionID() {
        return sessionID;
    }

    public boolean getSuccess() {return success;}

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
