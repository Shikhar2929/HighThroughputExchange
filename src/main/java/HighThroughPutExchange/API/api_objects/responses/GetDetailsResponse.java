package HighThroughPutExchange.API.api_objects.responses;

public class GetDetailsResponse {
    private boolean auth;
    private boolean success;
    private String userDetails;

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

    public String getUserDetails() {
        return userDetails;
    }
    public GetDetailsResponse(boolean auth, boolean success, String userDetails) {
        this.auth = auth;
        this.success = success;
        this.userDetails = userDetails;
    }
}
