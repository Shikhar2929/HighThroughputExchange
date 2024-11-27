package HighThroughPutExchange.MatchingEngine;

import java.util.HashMap;
import java.util.Map;
public class UserList {
    private Map<String, Double> userBalances = new HashMap<>(); // UserName -> Balance
    private Map<String, Map<String, Double>> quantities = new HashMap<>(); // Ticker -> Quantity
    public double getUserBalance(String username) {
        return userBalances.getOrDefault(username, 0.0);
    }
    public double getUserVolume(String username, String ticker) {
        if (!quantities.containsKey(username)) return 0.0;
        return quantities.get(username).getOrDefault(ticker, 0.0);
    }
    public boolean initializeUser(String username, double balance) {
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
    public boolean validQuantity(String username, String ticker, double volumeTraded) {
        if (!quantities.containsKey(username)) return false;
        double volumeOwned = quantities.get(username).getOrDefault(ticker, 0.0);
        return volumeOwned >= volumeTraded;
    }
    public double getValidVolume(String username, double price) {
        double currentBalance = getUserBalance(username);
        return Math.floor(currentBalance / price);
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
}

