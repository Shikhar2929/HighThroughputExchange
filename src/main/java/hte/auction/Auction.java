package hte.auction;

import hte.matchingengine.MatchingEngine;

public class Auction {
    private int bestBid;
    private String bestUser;
    private MatchingEngine matchingEngine;
    private final int MAX_BID = 100000;

    public int getMaxBid() {
        return MAX_BID;
    }

    public Auction(MatchingEngine matchingEngine) {
        bestBid = 0;
        bestUser = "";
        this.matchingEngine = matchingEngine;
    }

    public void reset() {
        bestBid = 0;
        bestUser = "";
    }

    public boolean isValid(String user, int bid) {
        if (bid > MAX_BID) {
            return false;
        }
        return true;
    }

    public boolean placeBid(String user, int bid) {
        if (bid > bestBid) {
            bestBid = bid;
            bestUser = user;
        }
        return true;
    }

    public String getBestUser() {
        return bestUser;
    }

    public int getBestBid() {
        return bestBid;
    }

    public void executeAuction() {
        matchingEngine.executeAuction(bestUser, bestBid);
    }
}
