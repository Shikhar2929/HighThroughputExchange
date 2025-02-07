package HighThroughPutExchange.API.api_objects.responses;

public class MarketOrderResponse {
    private String message;

    public MarketOrderResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
