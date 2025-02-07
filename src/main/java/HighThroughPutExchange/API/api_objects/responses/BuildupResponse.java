package HighThroughPutExchange.API.api_objects.responses;

public class BuildupResponse {
    private String message;
    private String sessionToken;
    private String orderBookData;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    public String getOrderBookData() {
        return orderBookData;
    }
    public BuildupResponse(String message, String sessionToken, String orderBookData) {
        this.message = message;
        this.sessionToken = sessionToken;
        this.orderBookData = orderBookData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOrderBookData(String orderBookData) {
        this.orderBookData = orderBookData;
    }
}
