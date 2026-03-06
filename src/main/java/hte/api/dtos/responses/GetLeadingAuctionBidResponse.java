package hte.api.dtos.responses;

public class GetLeadingAuctionBidResponse extends AbstractMessageResponse {
    private String firstUser;
    private double firstBid;
    private String secondUser;
    private double secondBid;

    public GetLeadingAuctionBidResponse(String message) {
        this(message, "", 0, "", 0);
    }

    public GetLeadingAuctionBidResponse(
            String message,
            String firstUser,
            double firstBid,
            String secondUser,
            double secondBid) {
        super(message);
        this.firstUser = firstUser;
        this.firstBid = firstBid;
        this.secondUser = secondUser;
        this.secondBid = secondBid;
    }

    public String getFirstUser() {
        return firstUser;
    }

    public void setFirstUser(String firstUser) {
        this.firstUser = firstUser;
    }

    public double getFirstBid() {
        return firstBid;
    }

    public void setFirstBid(double firstBid) {
        this.firstBid = firstBid;
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
