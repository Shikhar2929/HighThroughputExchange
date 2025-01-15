package HighThroughPutExchange.MatchingEngine;

import java.util.HashMap;
import java.util.Map;
public class UserList {
    private Map<String, Double> userBalances = new HashMap<>(); // UserName -> Balance
    private Map<String, Map<String, Double>> quantities = new HashMap<>(); // Ticker -> Quantity
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
        quantities.get(username).put(ticker, quantity);
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
        if (infinite)
            return positionLimit - getUserVolume(username, ticker);
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
        if (infinite)
            return positionLimit + getUserVolume(username, ticker);
        double currentBalance = getUserVolume(username, ticker);
        return currentBalance;
    }
    public boolean adjustUserBalance(String username, double delta) {
        double currentBalance = getUserBalance(username);
        double newBalance = currentBalance + delta;
        if (newBalance < 0) {
            return false;
        }
        userBalances.put(username, newBalance);
        return true;
    }
    public boolean adjustUserTickerBalance(String username, String ticker, double delta) {
        double currentBalance = getUserVolume(username, ticker);
        double newBalance = currentBalance + delta;
        if (newBalance < 0) {
            return false;
        }
        if (!quantities.containsKey(username)) return false;
        quantities.get(username).put(ticker, newBalance);
        return true;
    }
    public boolean getMode() {
        return infinite;
    }
}

