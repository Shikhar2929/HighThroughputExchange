package HighThroughPutExchange.common;

public enum Message {
    SUCCESS(0, "Success!"),
    AUTHENTICATION_FAILED(1, "Authentication failed."),
    RATE_LIMITED(2, "Rate Limited. Please stop spamming."),
    TRADE_LOCKED(3, "Trading is currently locked"),
    BAD_INPUT(4, "Bad input. Please check your parameters."),
    AUCTION_LOCKED(5, "Auctioning is currently locked."),
    POSITION_LIMIT_EXCEEDED(6, "Position limit has been exceeded."),
    INSUFFICIENT_BALANCE(7, "Insufficient balance."),
    INVALID_SEQ_NUM(8, "Update sequence number is too old or doesn't exist.");

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private final int errorCode;
    private final String errorMessage;

    Message(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return String.format(
                "{\"errorCode\": %d, \"errorMessage\": \"%s\"}", errorCode, errorMessage);
    }
}
