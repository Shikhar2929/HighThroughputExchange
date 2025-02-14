package HighThroughPutExchange.API.api_objects.requests;

public class BidAuctionRequest extends BasePrivateRequest {

    private double bid;

    public BidAuctionRequest(String username, String sessionToken, double bid) {
        super(username, sessionToken);
        this.bid = bid;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }
}
