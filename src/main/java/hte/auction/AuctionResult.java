package hte.auction;

public class AuctionResult {
    private final String firstUser;
    private final int firstBid;
    private final String secondUser;
    private final int secondBid;

    public AuctionResult(String firstUser, int firstBid, String secondUser, int secondBid) {
        this.firstUser = firstUser;
        this.firstBid = firstBid;
        this.secondUser = secondUser;
        this.secondBid = secondBid;
    }

    public String getFirstUser() {
        return firstUser;
    }

    public int getFirstBid() {
        return firstBid;
    }

    public String getSecondUser() {
        return secondUser;
    }

    public int getSecondBid() {
        return secondBid;
    }
}
