package HighThroughPutExchange.API.api_objects.responses;

public class ShutdownResponse {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ShutdownResponse(String message) {
        this.message = message;
    }
}
