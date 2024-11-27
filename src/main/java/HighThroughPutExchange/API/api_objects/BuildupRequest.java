package HighThroughPutExchange.API.api_objects;

public class BuildupRequest {
    private String username;
    private String apiKey;


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
