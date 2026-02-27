package hte.auction;

public class AuctionResult {
    private final String user;
    private final int bid;

    public AuctionResult(String user, int bid) {
        this.user = user;
        this.bid = bid;
    }

    public String getUser() {
        return user;
    }

    public int getBid() {
        return bid;
    }
}
