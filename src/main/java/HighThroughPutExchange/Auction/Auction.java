package HighThroughPutExchange.Auction;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
public class Auction {
    private double bestBid;
    private String bestUser;
    private MatchingEngine matchingEngine;
    public Auction(MatchingEngine matchingEngine) {
        bestBid = 0.0;
        bestUser = "";
        this.matchingEngine = matchingEngine;
    }
    public void reset() {
        bestBid = 0.0;
        bestUser = "";
    }

    public boolean isValid(String user, double bid) {
        if (matchingEngine.getUserBalance(user) < bid) {
            return false;
        }
        return true;
    }

    public boolean placeBid(String user, double bid) {
        if (!isValid(user, bid)) {
            return false;
        }
        if (bid > bestBid) {
            bestBid = bid;
            bestUser = user;
        }
        return true;
    }

    public String getBestUser() {return bestUser;}

    public double getBestBid() {
        return bestBid;
    }

    public void executeAuction() {
        matchingEngine.executeAuction(bestUser, bestBid);
    }
}
