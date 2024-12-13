package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;

public class AddUserRequest extends BaseAdminRequest {
    @NotNull
    private String username;
    @NotNull
    private String name;
    @NotNull
    private String email;


    public AddUserRequest(String adminUsername, String adminPassword, String username, String name, String email) {
        super(adminUsername, adminPassword);
        this.username = username;
        this.name = name;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
