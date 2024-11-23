package HighThroughPutExchange.API.api_objects;

public class AdminLoginRequest {
    private String username;
    private String password;

    public AdminLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
