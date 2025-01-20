package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorCode {

    SUCCESS,
    AUTHENTICATION_ERROR,
    RATE_LIMIT_ERROR,
    INVALID_INPUT_ERROR,
    INTERNAL_SERVER_ERROR;

    @JsonValue
    public int getValue() {
        return ordinal();
    }
}
