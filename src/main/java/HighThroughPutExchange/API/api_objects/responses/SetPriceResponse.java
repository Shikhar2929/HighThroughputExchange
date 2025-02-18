package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class SetPriceResponse {
    @JsonRawValue
    private String message;
    public SetPriceResponse(String message) {
        this.message = message;
    }
}
