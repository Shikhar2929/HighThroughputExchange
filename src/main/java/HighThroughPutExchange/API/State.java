package HighThroughPutExchange.API;

public enum State {
    STOP,
    TRADE,
    AUCTION;

    /*
    track state by ordinal;
        0 is STOP
        1 is TRADE
        2 is AUCTION
     */
}
