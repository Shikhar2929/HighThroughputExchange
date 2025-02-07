package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Message {

    SUCCESS("Success!"),
    RATE_LIMITED("Rate Limited. Please stop spamming."),
    AUTHENTICATION_FAILED("Authentication failed."),
    TRADE_LOCKED("Trading is currently locked"),
    BAD_INPUT("Bad input. Please check your parameters."),
    AUCTION_LOCKED("Auctioning is currently locked.");

    private final String message;

    Message(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
