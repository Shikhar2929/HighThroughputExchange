package HighThroughPutExchange.API.api_objects;

public class AddUserRequest {
    private String adminUsername;
    private String adminPassword;
    private String username;
    private String name;
    private String email;


    public AddUserRequest(String adminUsername, String adminPassword, String username, String name, String email) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.username = username;
        this.name = name;
        this.email = email;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
