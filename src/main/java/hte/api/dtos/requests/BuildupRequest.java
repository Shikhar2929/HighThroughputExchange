package hte.api.dtos.requests;

import jakarta.validation.constraints.NotNull;

public class BuildupRequest {
    @NotNull private String username;
    @NotNull private String apiKey;

    public BuildupRequest(String username, String apiKey) {
        this.username = username;
        this.apiKey = apiKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
