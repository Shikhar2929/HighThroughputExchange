package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class MarketOrderResponse {
    @JsonRawValue
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
