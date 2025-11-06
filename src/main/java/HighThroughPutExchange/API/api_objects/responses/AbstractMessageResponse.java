package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * Minimal holder for message-centric responses so that serialization flags stay
 * consistent.
 */
public abstract class AbstractMessageResponse {
    @JsonRawValue
    private String message;

    protected AbstractMessageResponse() {
        // for deserialization
    }

    protected AbstractMessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
