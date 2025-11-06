package HighThroughPutExchange.API.api_objects.responses;

public class AddUserResponse extends AbstractMessageResponse {
    private String apiKey;
    private String apiKey2;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public AddUserResponse(String message, String apiKey) {
        this(message, apiKey, apiKey);
    }

    public AddUserResponse(String message, String apiKey, String apiKey2) {
        super(message);
        this.apiKey = apiKey;
        this.apiKey2 = apiKey2;
    }

    public String getApiKey2() {
        return apiKey2;
    }

    public void setApiKey2(String apiKey2) {
        this.apiKey2 = apiKey2;
    }
}
