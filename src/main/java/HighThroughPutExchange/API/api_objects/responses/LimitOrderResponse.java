package HighThroughPutExchange.API.api_objects.responses;

public class LimitOrderResponse {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LimitOrderResponse(String message) {
        this.message = message;
    }
}
