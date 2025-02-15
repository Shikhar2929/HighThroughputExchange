package HighThroughPutExchange.MatchingEngine;

import org.json.JSONObject;

import java.util.*;

public class UserList {
    private Map<String, Double> userBalances = new HashMap<>(); // UserName -> Balance
    private Map<String, Map<String, Double>> quantities = new HashMap<>(); // Actual Amount Owned
    private Map<String, Map<String, Double>> bidSize = new HashMap<>();
    private Map<String, Map<String, Double>> askSize = new HashMap<>();
    private boolean infinite = false;
    private double positionLimit = 0.0;

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }
    public void setPositionLimit(double positionLimit) {
        this.positionLimit = positionLimit;
    }


    public double getUserBalance(String username) {
        return userBalances.getOrDefault(username, 0.0);
    }
    public double getUserVolume(String username, String ticker) {
        if (!quantities.containsKey(username)) return 0.0;
        return quantities.get(username).getOrDefault(ticker, 0.0);
    }
    /*
    initialize User method, intended for infinite initialization
     */
    public boolean initializeUser(String username) {
        //Initialize User for infinite balance case
        System.out.println("Initializing...");
        if (userBalances.containsKey(username) || !infinite) return false;
        userBalances.put(username, 0.0);
        return true;
    }
    /*
    initialize User method, intended for finite initialization
     */
    public boolean initializeUser(String username, double balance) {
        if (infinite) return initializeUser(username);
        if (userBalances.containsKey(username)) return false;
        userBalances.put(username, balance);
        return true;
    }
    public boolean initializeUserQuantity(String username, String ticker, double quantity) {
        if (!quantities.containsKey(username)) {
            quantities.put(username, new HashMap<>());
        }
        if (!askSize.containsKey(username))
            askSize.put(username, new HashMap<>());
        if (!bidSize.containsKey(username))
            bidSize.put(username, new HashMap<>());
        quantities.get(username).put(ticker, quantity);
        askSize.get(username).put(ticker, 0.0);
        bidSize.get(username).put(ticker, 0.0);
        return true;
    }
    public boolean validUser(String username) {
        return userBalances.containsKey(username);
    }
    public boolean validAskQuantity(String username, String ticker, double volumeTraded) {
        if (!quantities.containsKey(username)) return false;
        return volumeTraded <= getValidAskVolume(username, ticker);
    }
    /*
    getValidBidVolume method, for both finite and infinite volumes
     */
    public double getValidBidVolume(String username, String ticker, double price) {
        if (infinite) {
            double returnVal = positionLimit - getUserVolume(username, ticker) - bidSize.get(username).getOrDefault(ticker, 0.0);
            System.out.printf("Return Value: %f\n", returnVal);
            System.out.printf("Volume: %f\n", getUserVolume(username, ticker));
            System.out.printf("Bid Size %f\n", bidSize.get(username).getOrDefault(ticker, 0.0));
            return returnVal;
        }
        double currentBalance = getUserBalance(username);
        if (price != 0.0)
            return currentBalance / price;
        else
            return 0.0;
    }
    public boolean validBidParameters(String username, Order order) {
        return getValidBidVolume(username, order.ticker, order.price) >= order.volume;
    }
    public double getValidAskVolume(String username, String ticker) {
        if (infinite) {
            return positionLimit + getUserVolume(username, ticker) - askSize.get(username).getOrDefault(ticker, 0.0);
        }
        return getUserVolume(username, ticker);
    }
    public boolean adjustUserBalance(String username, double delta) {
        double currentBalance = getUserBalance(username);
        double newBalance = currentBalance + delta;
        if (newBalance < 0 && !infinite) {
            System.out.println("NEGATIVE AND NOT INFINITE");
            return false;
        }
        userBalances.put(username, newBalance);
        return true;
    }
    public boolean adjustUserTickerBalance(String username, String ticker, double delta) {
        double currentBalance = getUserVolume(username, ticker);
        double newBalance = currentBalance + delta;
        if (newBalance < 0 && !infinite) {
            System.out.println("NEGATIVE AND NOT INFINITE");
            return false;
        }
        if (!quantities.containsKey(username)) return false;
        quantities.get(username).put(ticker, newBalance);
        return true;
    }
    public boolean adjustUserAskBalance(String username, String ticker, double delta) {
        if (!askSize.containsKey(username)) {
            return false;
        }
        if (!askSize.get(username).containsKey(ticker))
            return false;
        askSize.get(username).compute(ticker, (k, currentBalance) -> currentBalance + delta);
        return true;
    }
    public boolean adjustUserBidBalance(String username, String ticker, double delta) {
        if (!bidSize.containsKey(username)) {
            return false;
        }
        if (!bidSize.get(username).containsKey(ticker)) {
            return false;
        }
        bidSize.get(username).compute(ticker, (k, currentBalance) -> currentBalance + delta);
        return true;
    }

    public boolean getMode() {
        return infinite;
    }
    public JSONObject getUserDetailsAsJson(String username, Map<String, Double> prices) {
        JSONObject userJson = new JSONObject();

        // Check if the user exists
        if (!validUser(username)) {
            userJson.put("error", "User not found");
            return userJson;
        }

        // Add balance to JSON
        double balance = getUserBalance(username);
        userJson.put("username", username);
        userJson.put("balance", balance);
        userJson.put("pnl", getUnrealizedPnl(username, prices));
        // Add positions to JSON
        Map<String, Double> userPositions = quantities.getOrDefault(username, new HashMap<>());
        JSONObject positionsJson = new JSONObject(userPositions);
        userJson.put("positions", positionsJson);

        return userJson;
    }
    public double getUnrealizedPnl(String username, Map<String, Double> prices) {
        double pnl = getUserBalance(username);
        for (Map.Entry<String, Double> entry : prices.entrySet()) {
            pnl += quantities.get(username).get(entry.getKey()) * entry.getValue();
        }
        return pnl;
    }

    // todo: consider sorting only in the frontend
    public ArrayList<LeaderboardEntry> getLeaderboard(Map<String, Double> prices) {
        ArrayList<LeaderboardEntry> output = new ArrayList<>();
        for (String username: userBalances.keySet()) {
            output.add(new LeaderboardEntry(username, getUnrealizedPnl(username, prices)));
        }
        Collections.sort(output);
        return output;
    }
}

