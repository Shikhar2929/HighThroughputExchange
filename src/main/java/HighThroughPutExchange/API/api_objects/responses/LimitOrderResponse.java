package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class LimitOrderResponse {
    @JsonRawValue
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
