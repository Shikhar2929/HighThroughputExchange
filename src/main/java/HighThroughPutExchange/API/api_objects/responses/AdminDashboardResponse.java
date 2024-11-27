package HighThroughPutExchange.API.api_objects.responses;

public class AdminDashboardResponse {
    private boolean auth;
    private boolean success;
    private String data;


    public AdminDashboardResponse(boolean auth, boolean success, String data) {
        this.auth = auth;
        this.success = success;
        this.data = data;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
