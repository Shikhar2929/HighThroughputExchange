package HighThroughPutExchange.MatchingEngine;

import org.json.JSONObject;

import java.util.*;

public class UserList {
    private Map<String, Long> userBalances = new HashMap<>(); // UserName -> Balance
    private Map<String, Map<String, Integer>> quantities = new HashMap<>(); // Actual Amount Owned
    private Map<String, Map<String, Double>> sumPrices = new HashMap<>();
    private Map<String, Map<String, Integer>> bidSize = new HashMap<>();
    private Map<String, Map<String, Integer>> askSize = new HashMap<>();
    private boolean infinite = false;
    private int positionLimit = 0;

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }
    public void setPositionLimit(int positionLimit) {
        this.positionLimit = positionLimit;
    }


    public long getUserBalance(String username) {
        return userBalances.getOrDefault(username, (long) 0);
    }
    public int getUserVolume(String username, String ticker) {
        if (!quantities.containsKey(username)) return 0;
        return quantities.get(username).getOrDefault(ticker, 0);
    }
    /*
    initialize User method, intended for infinite initialization
     */
    public boolean initializeUser(String username) {
        //Initialize User for infinite balance case
        System.out.println("Initializing...");
        if (userBalances.containsKey(username) || !infinite) return false;
        userBalances.put(username, (long) 0);
        return true;
    }
    /*
    initialize User method, intended for finite initialization
     */
    public boolean initializeUser(String username, int balance) {
        if (infinite) return initializeUser(username);
        if (userBalances.containsKey(username)) return false;
        userBalances.put(username, (long) balance);
        return true;
    }
    public boolean initializeUserQuantity(String username, String ticker, int quantity) {
        if (!quantities.containsKey(username)) {
            quantities.put(username, new HashMap<>());
        }
        if (!askSize.containsKey(username))
            askSize.put(username, new HashMap<>());
        if (!bidSize.containsKey(username))
            bidSize.put(username, new HashMap<>());
        if (!sumPrices.containsKey(username))
            sumPrices.put(username, new HashMap<>());
        quantities.get(username).put(ticker, quantity);
        sumPrices.get(username).put(ticker, 0.0);
        askSize.get(username).put(ticker, 0);
        bidSize.get(username).put(ticker, 0);
        return true;
    }
    public boolean validUser(String username) {
        return userBalances.containsKey(username);
    }
    public boolean validAskQuantity(String username, String ticker, int volumeTraded) {
        if (!quantities.containsKey(username)) return false;
        return volumeTraded <= getValidAskVolume(username, ticker);
    }
    /*
    getValidBidVolume method, for both finite and infinite volumes
     */
    public int getValidBidVolume(String username, String ticker, int price) {
        if (infinite) {
            int returnVal = positionLimit - getUserVolume(username, ticker) - bidSize.get(username).getOrDefault(ticker, 0);
            return returnVal;
        }
        int currentBalance = (int) getUserBalance(username);
        if (price != 0.0)
            return currentBalance / price;
        else
            return 0;
    }
    public boolean validBidParameters(String username, Order order) {
        return getValidBidVolume(username, order.ticker, order.price) >= order.volume;
    }
    public int getValidAskVolume(String username, String ticker) {
        if (infinite) {
            return positionLimit + getUserVolume(username, ticker) - askSize.get(username).getOrDefault(ticker, 0);
        }
        return getUserVolume(username, ticker);
    }
    public boolean adjustUserBalance(String username, int delta) {
        long currentBalance = getUserBalance(username);
        long newBalance = currentBalance + delta;
        if (newBalance < 0 && !infinite) {
            System.out.println("NEGATIVE AND NOT INFINITE");
            return false;
        }
        userBalances.put(username, newBalance);
        return true;
    }
    public boolean adjustUserTickerBalance(String username, String ticker, int delta, int price) {
        int currentBalance = getUserVolume(username, ticker);
        int newBalance = currentBalance + delta;
        //System.out.printf("%s: %d %d\n", username, delta, price);
        double currSum = sumPrices.get(username).get(ticker);
        //System.out.println(currSum);
        if (!quantities.containsKey(username)) return false;
        if (delta < 0 && currentBalance > 0 && Math.abs(currentBalance) > Math.abs(delta)) {
            double oldPrice = currSum / currentBalance;
            //System.out.println(oldPrice);
            currSum += oldPrice * delta;
            sumPrices.get(username).put(ticker, currSum);
        }
        else if (delta > 0 && currentBalance < 0 && Math.abs(currentBalance) > Math.abs(delta)) {
            double oldPrice = currSum / currentBalance;
            currSum += oldPrice * delta;
            sumPrices.get(username).put(ticker, currSum);
        }
        else if (delta < 0 && currentBalance >= 0 && Math.abs(currentBalance) <= Math.abs(delta)) {
            currSum = (currentBalance + delta) * price;
            //System.out.printf("New Sum: %.2f\n", currSum);
            sumPrices.get(username).put(ticker, currSum);
        }
        else if (delta > 0 && currentBalance <= 0 && Math.abs(currentBalance) <= Math.abs(delta)) {
            currSum = (currentBalance + delta) * price;
            //System.out.printf("New Sum: %.2f\n", currSum);
            sumPrices.get(username).put(ticker, currSum);
        }
        else {
            currSum += delta * price;
            sumPrices.get(username).put(ticker, currSum);
        }
        quantities.get(username).put(ticker, newBalance);
        return true;
    }
    public boolean adjustUserAskBalance(String username, String ticker, int delta) {
        if (!askSize.containsKey(username)) {
            return false;
        }
        if (!askSize.get(username).containsKey(ticker))
            return false;
        askSize.get(username).compute(ticker, (k, currentBalance) -> currentBalance + delta);
        return true;
    }
    public boolean adjustUserBidBalance(String username, String ticker, int delta) {
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
    public JSONObject getUserDetailsAsJson(String username, Map<String, Integer> prices) {
        JSONObject userJson = new JSONObject();

        // Check if the user exists
        if (!validUser(username)) {
            userJson.put("error", "User not found");
            return userJson;
        }

        // Add balance to JSON
        long balance = getUserBalance(username);
        userJson.put("username", username);
        userJson.put("balance", balance);
        userJson.put("pnl", getUnrealizedPnl(username, prices));
        // Add positions to JSON
        //Map<String, Integer> userPositions = quantities.getOrDefault(username, new HashMap<>());
        JSONObject positionsJson = new JSONObject();
        for (String ticker : quantities.getOrDefault(username, new HashMap<>()).keySet()) {
            int quantity = quantities.get(username).getOrDefault(ticker, 0);
            double sumPrice = sumPrices.getOrDefault(username, new HashMap<>()).getOrDefault(ticker, 0.0);
            double avgPrice = quantity != 0.0 ? (double) sumPrice / (double) quantity : 0.0;

            JSONObject tickerDetails = new JSONObject();
            tickerDetails.put("quantity", quantity);
            tickerDetails.put("averagePrice", avgPrice);

            positionsJson.put(ticker, tickerDetails);
        }
        userJson.put("positions", positionsJson);
        userJson.put("positions", positionsJson);

        return userJson;
    }
    public long getUnrealizedPnl(String username, Map<String, Integer> prices) {
        long pnl = getUserBalance(username);
        for (Map.Entry<String, Integer> entry : prices.entrySet()) {
            try {
                pnl += quantities.get(username).get(entry.getKey()) * entry.getValue();
            }
            catch (Exception e){
                if (!quantities.containsKey(username)) {
                    System.out.println("username not found");
                }
                else if (!quantities.get(username).containsKey(entry.getKey())) {
                    System.out.println("Weird Ticker Not Found");
                }
                else
                    System.out.println("Weird Exception");
            }
        }
        return pnl;
    }

    // todo: consider sorting only in the frontend
    public ArrayList<LeaderboardEntry> getLeaderboard(Map<String, Integer> prices) {
        ArrayList<LeaderboardEntry> output = new ArrayList<>();
        for (String username: userBalances.keySet()) {
            output.add(new LeaderboardEntry(username, getUnrealizedPnl(username, prices)));
        }
        Collections.sort(output);
        return output;
    }
}

