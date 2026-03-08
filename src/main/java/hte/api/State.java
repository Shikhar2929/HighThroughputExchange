package hte.api;

public enum State {
    STOP,
    TRADE,
    TRADE_WITH_AUCTION,
    AUCTION;

    /*
     * track state by ordinal; 0 is STOP, 1 is TRADE, 2 is TRADE_WITH_AUCTION, 3 is AUCTION
     */

    public boolean isTradingAllowed() {
        return this == TRADE || this == TRADE_WITH_AUCTION;
    }

    public boolean isAuctionAllowed() {
        return this == AUCTION || this == TRADE_WITH_AUCTION;
    }
}
