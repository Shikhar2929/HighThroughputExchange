package hte.auction;

import hte.matchingengine.MatchingEngine;
import java.util.HashMap;
import java.util.Map;

public class Auction {
    private final Map<String, Integer> userBids;
    private MatchingEngine matchingEngine;
    private final int MAX_BID = 100000;

    public int getMaxBid() {
        return MAX_BID;
    }

    public Auction(MatchingEngine matchingEngine) {
        userBids = new HashMap<>();
        this.matchingEngine = matchingEngine;
    }

    public void reset() {
        userBids.clear();
    }

    public boolean isValid(String user, int bid) {
        if (bid > MAX_BID) {
            return false;
        }
        return true;
    }

    public boolean placeBid(String user, int bid) {
        userBids.put(user, bid);
        return true;
    }

    public String getBestUser() {
        String bestUser = "";
        int bestBid = 0;
        for (Map.Entry<String, Integer> entry : userBids.entrySet()) {
            if (entry.getValue() > bestBid) {
                bestBid = entry.getValue();
                bestUser = entry.getKey();
            }
        }
        return bestUser;
    }

    public int getBestBid() {
        int bestBid = 0;
        for (int bid : userBids.values()) {
            if (bid > bestBid) {
                bestBid = bid;
            }
        }
        return bestBid;
    }

    public void executeAuction() {
        matchingEngine.executeAuction(getBestUser(), getBestBid());
    }
}
