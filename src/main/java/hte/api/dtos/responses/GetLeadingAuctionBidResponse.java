package hte.api.dtos.responses;

public class GetLeadingAuctionBidResponse extends AbstractMessageResponse {
    private String user;
    private double bid;
    private String secondUser;
    private double secondBid;

    public GetLeadingAuctionBidResponse(String message) {
        this(message, "", 0, "", 0);
    }

    public GetLeadingAuctionBidResponse(
            String message, String user, double bid, String secondUser, double secondBid) {
        super(message);
        this.user = user;
        this.bid = bid;
        this.secondUser = secondUser;
        this.secondBid = secondBid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public String getSecondUser() {
        return secondUser;
    }

    public void setSecondUser(String secondUser) {
        this.secondUser = secondUser;
    }

    public double getSecondBid() {
        return secondBid;
    }

    public void setSecondBid(double secondBid) {
        this.secondBid = secondBid;
    }
}
