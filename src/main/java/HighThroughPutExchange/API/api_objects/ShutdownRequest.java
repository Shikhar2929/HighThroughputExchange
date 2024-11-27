package HighThroughPutExchange.API.api_objects;

public class ShutdownRequest {

    private String adminUsername;
    private String adminPassword;

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public ShutdownRequest(String adminUsername, String adminPassword) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }
}
