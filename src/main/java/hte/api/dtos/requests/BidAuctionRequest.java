package hte.api.dtos.requests;

public class BidAuctionRequest extends BasePrivateRequest {

    private int bid;

    public BidAuctionRequest(String username, String sessionToken, int bid) {
        super(username, sessionToken);
        this.bid = bid;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }
}
