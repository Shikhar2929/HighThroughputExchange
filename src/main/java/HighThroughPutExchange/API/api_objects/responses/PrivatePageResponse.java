package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class PrivatePageResponse {
    @JsonRawValue
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
