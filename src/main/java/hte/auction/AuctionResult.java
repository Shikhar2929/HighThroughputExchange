package hte.auction;

public class AuctionResult {
    private final String user;
    private final int bid;
    private final String secondUser;
    private final int secondBid;

    public AuctionResult(String user, int bid, String secondUser, int secondBid) {
        this.user = user;
        this.bid = bid;
        this.secondUser = secondUser;
        this.secondBid = secondBid;
    }

    public String getUser() {
        return user;
    }

    public int getBid() {
        return bid;
    }

    public String getSecondUser() {
        return secondUser;
    }

    public int getSecondBid() {
        return secondBid;
    }
}
