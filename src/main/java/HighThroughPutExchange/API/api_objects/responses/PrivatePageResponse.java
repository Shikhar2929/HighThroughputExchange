package HighThroughPutExchange.API.api_objects.responses;

public class PrivatePageResponse {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PrivatePageResponse(String message) {
        this.message = message;
    }
}
