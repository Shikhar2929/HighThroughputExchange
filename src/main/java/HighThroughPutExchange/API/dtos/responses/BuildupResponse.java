package HighThroughPutExchange.api.dtos.responses;

public class BuildupResponse extends AbstractMessageResponse {
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
        super(message);
        this.sessionToken = sessionToken;
        this.orderBookData = orderBookData;
    }

    public void setOrderBookData(String orderBookData) {
        this.orderBookData = orderBookData;
    }
}
