package hte.common;

public enum Message {
    SUCCESS(0, "Success!"),
    PARTIAL_FILL(0, "Partially filled."),

    AUTHENTICATION_FAILED(1, "Authentication failed."),
    RATE_LIMITED(2, "Rate Limited. Please stop spamming."),
    TRADE_LOCKED(3, "Trading is currently locked"),
    BAD_INPUT(4, "Bad input. Please check your parameters."),
    AUCTION_LOCKED(5, "Auctioning is currently locked."),
    POSITION_LIMIT_EXCEEDED(6, "Position limit has been exceeded."),
    INSUFFICIENT_BALANCE(7, "Insufficient balance."),
    INVALID_SEQ_NUM(8, "Update sequence number is too old or doesn't exist."),
    USER_NOT_INITIALIZED(9, "User not initialized on server yet. Please retry."),
    UNKNOWN_TICKER(10, "Unknown ticker."),
    INVALID_VOLUME(11, "Invalid volume. Must be > 0."),
    INVALID_PRICE(12, "Invalid price. Must be > 0."),
    INVALID_ORDER_ID(13, "Invalid orderId. Must be a positive integer."),
    ORDER_NOT_FOUND(14, "Order not found."),
    INSUFFICIENT_TICKER_BALANCE(15, "Insufficient ticker balance."),
    SERVER_MISCONFIGURED(16, "Server is misconfigured. Please contact an administrator."),
    INTERNAL_ERROR(17, "Internal server error."),
    NO_LIQUIDITY(18, "No liquidity. No matching orders available.");

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

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public String toString() {
        return String.format(
                "{\"errorCode\": %d, \"errorMessage\": \"%s\"}",
                errorCode, jsonEscape(errorMessage));
    }
}
