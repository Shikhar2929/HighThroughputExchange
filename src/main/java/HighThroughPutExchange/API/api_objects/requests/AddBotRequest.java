package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;

public class AddBotRequest extends BaseAdminRequest {
    @NotNull
    private String username;

    public AddBotRequest(
            String adminUsername, String adminPassword, String username, String name, String email) {
        super(adminUsername, adminPassword);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
