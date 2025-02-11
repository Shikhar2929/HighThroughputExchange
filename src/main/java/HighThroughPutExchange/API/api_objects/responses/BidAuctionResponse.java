package HighThroughPutExchange.API.api_objects.responses;

public class BidAuctionResponse {

    private String message;

    public BidAuctionResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
