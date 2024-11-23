package HighThroughPutExchange.MatchingEngine;

import java.util.HashMap;
import java.util.Map;
public class UserList {
    private Map<String, Double> userBalances = new HashMap<>(); // UserName -> Balance
    private Map<String, Map<String, Double>> userAssets = new HashMap<>(); // UserName -> (Asset -> Quantity)
    public double getUserBalance(String username) {
        return userBalances.getOrDefault(username, 0.0);
    }
    public boolean initializeUser(String username, double balance) {
        if (userBalances.containsKey(username)) return false;
        userBalances.put(username, balance);
        return true;
    }
    public boolean validUser(String username) {
        return userBalances.containsKey(username);
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

}

