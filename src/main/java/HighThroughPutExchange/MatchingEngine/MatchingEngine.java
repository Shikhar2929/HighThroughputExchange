package HighThroughPutExchange.MatchingEngine;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.Iterator;

import HighThroughPutExchange.Common.TaskFuture;
import com.fasterxml.jackson.databind.ObjectMapper;

// todo: get rid of usage of JSON library
 import org.json.JSONArray;
 import org.json.JSONObject;

public class MatchingEngine {
    private Map<String, OrderBook> orderBooks = new HashMap<>();
    private Map<String, Map<Long, Order>> userOrders = new HashMap<>(); // UserName, OrderId, Order
    private UserList userList = new UserList();
    private long orderID = 0;
    public MatchingEngine() {
        userList.setInfinite(false);
    }
    public MatchingEngine(boolean initialize){
        if (initialize)
            initializeGameMode();
    }
    public MatchingEngine(double positionLimit) {
        userList.setInfinite(true);
        userList.setPositionLimit(positionLimit);
    }
    public String serializeOrderBooks() {
        ObjectMapper mapper = new ObjectMapper();
        for (String key : orderBooks.keySet()) {
            orderBooks.get(key).printOrderBook();
        }
        try {
            return mapper.writeValueAsString(orderBooks);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private void initializeGameMode() {
        try {
            FileReader reader = new FileReader("config.json");
            StringBuilder content = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                content.append((char) i);
            }
            reader.close();

            // Parse JSON content
            JSONObject configData = new JSONObject(content.toString());
            String mode = configData.getString("mode");
            if (mode.equals("finite"))
                userList.setInfinite(false);
            else {
                userList.setInfinite(true);
                JSONObject defaults = configData.getJSONObject("defaults");
                double positionLimit = defaults.getDouble("positionLimit");
                userList.setPositionLimit(positionLimit);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean initializeUserBalance(String username, double balance) {
        System.out.println("Initializing: " + username);
        return userList.initializeUser(username, balance);
    }
    public boolean initializeUserTickerVolume(String username, String ticker, double volume) {
        System.out.println("Initializing: " + username + " with ticker: " + ticker);
        return userList.initializeUserQuantity(username, ticker, volume);
    }
    public double getUserBalance(String username) {
        return userList.getUserBalance(username);
    }
    public ArrayList getRecentTrades() {
        ArrayList<PriceChange> recentTrades = RecentTrades.getRecentTrades();
        //for (Trade trade : recentTrades)
        ///    System.out.println(trade);
        return recentTrades;
    }
    public double getTickerBalance(String username, String ticker) {
        return userList.getUserVolume(username, ticker);
    }
    public boolean initializeTicker(String ticker) {
        if (orderBooks.containsKey(ticker)) {
            return false;
        }
        orderBooks.put(ticker, new OrderBook());
        return true;
    }

    // todo: test and replace
    public boolean alternativeInitializeAllTickers() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            FileReader reader = new FileReader("config.json");
            OrderbookConfig configData = mapper.readerFor(OrderbookConfig.class).readValue(reader);
            reader.close();

            for (String ticker: configData.getDefaults().getTickers()) {
                System.out.println("Ticker: " + ticker);
                initializeTicker(ticker);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public boolean initializeAllTickers() {
        try {
            // Read the JSON file
            System.out.println("Current Working Directory: " + Paths.get("").toAbsolutePath());
            FileReader reader = new FileReader("config.json");
            StringBuilder content = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                content.append((char) i);
            }
            reader.close();
            JSONObject configData = new JSONObject(content.toString());
            JSONArray tickersArray = configData.getJSONObject("defaults").getJSONArray("tickers");
            for (int j = 0; j < tickersArray.length(); j++) {
                String ticker = (String) tickersArray.getString(j);
                System.out.println("Ticker: " + ticker);
                initializeTicker(ticker);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // todo test and replace
    public boolean alternativeInitializeUser(String user) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            FileReader reader = new FileReader("config.json");
            OrderbookConfig configData = mapper.readerFor(OrderbookConfig.class).readValue(reader);
            reader.close();

            System.out.println("Default Balance: " + configData.getDefaults().getDefaultBalance());
            for (String key: configData.getDefaults().getBalances().keySet()) {
                System.out.println("Ticker: " + key + ", Balance: " + configData.getDefaults().getBalances().get(key));
                initializeUserTickerVolume(user, key, configData.getDefaults().getBalances().get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean initializeUser(String user) {
        try {
            // Read the JSON file
            FileReader reader = new FileReader("config.json");
            StringBuilder content = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                content.append((char) i);
            }
            reader.close();

            // Parse JSON content
            JSONObject configData = new JSONObject(content.toString());
            String mode = configData.getString("mode");
            if (!userList.getMode()) {
                // Extract and process balances
                JSONObject defaults = configData.getJSONObject("defaults");
                double defaultBalance = defaults.getDouble("defaultBalance");
                JSONObject balances = defaults.getJSONObject("balances");
                System.out.println("Default Balance: " + defaultBalance);
                initializeUserBalance(user, defaultBalance);
                Iterator<String> keys = balances.keys();
                while (keys.hasNext()) {
                    String ticker = keys.next();
                    double balance = balances.getDouble(ticker);
                    System.out.println("Ticker: " + ticker + ", Balance: " + balance);
                    initializeUserTickerVolume(user, ticker, balance);
                }
            }
            else {
                JSONObject defaults = configData.getJSONObject("defaults");
                JSONObject balances = defaults.getJSONObject("balances");
                Iterator<String> keys = balances.keys();
                System.out.println("Infinite Mode");
                userList.initializeUser(user);
                while (keys.hasNext()) {
                    String ticker = keys.next();
                    double balance = balances.getDouble(ticker);
                    System.out.println("Ticker: " + ticker + ", Balance: " + balance);
                    initializeUserTickerVolume(user, ticker, balance);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public double getHighestBid(String ticker) {
        if (!orderBooks.containsKey(ticker)) return 0.0;
        TreeMap<Double, Deque<Order>> bids = orderBooks.get(ticker).bids;
        if (bids.isEmpty()) {
            return 0.0;
        }
        return bids.lastKey();
    }
    public double getLowestAsk(String ticker) {
        if (!orderBooks.containsKey(ticker)) return 0.0;
        TreeMap<Double, Deque<Order>> asks = orderBooks.get(ticker).asks;
        if (asks.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        return asks.firstKey();
    }
    protected void updateVolume(Map<Double, Double> volumeMap, double price, double delta, String ticker, Side side) {
        volumeMap.put(price, volumeMap.getOrDefault(price, 0.0) + delta);
        if (volumeMap.get(price) <= 0) {
            volumeMap.remove(price);
        }
        double newQuantity = volumeMap.getOrDefault(price, 0.0);
        RecentTrades.addTrade(ticker, price, newQuantity, side);
    }
    private boolean validateBidOrder(String user, Order order) {
        if (order.volume <= 0.0 || order.price <= 0.0 || order.status != Status.ACTIVE) {
            return false;
        }
        if (!orderBooks.containsKey(order.ticker)) {
            System.out.println("Bad Ticker");
            return false;
        }
        if (!userList.validUser(user)) {
            System.out.println("Bad User");
            return false;
        }
        if (!userList.validBidParameters(user, order)) {
            System.out.println("Invalid Volume Parameters");
            return false;
        }
        return true;
    }
    private boolean validateAskOrder(String user, Order order) {
        if (order.volume <= 0.0 || order.price <= 0.0 || order.status != Status.ACTIVE) {
            return false;
        }
        if (!orderBooks.containsKey(order.ticker)) {
            System.out.println("Bad Ticker");
            return false;
        }
        if (!userList.validUser(user)) {
            System.out.println("Bad User");
            return false;
        }
        if (!userList.validAskQuantity(user, order.ticker, order.volume)) {
            System.out.println("Insufficient Sell Funds");
            return false;
        }
        return true;
    }
    private void processBid(Deque<Order> orders, Map<Double, Double> askVolumes, Order aggressor) {
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
            }
            else if (order.volume > aggressor.volume) {
                double volumeTraded = aggressor.volume;
                updateVolume(askVolumes, order.price, -volumeTraded, order.ticker, Side.ASK);
                userList.adjustUserBalance(order.name, volumeTraded * order.price);
                userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                //System.out.println("Adjusting balance 1");

                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
                double volumeTraded = order.volume;
                updateVolume(askVolumes, order.price, -volumeTraded, order.ticker, Side.ASK);
                userList.adjustUserBalance(order.name, volumeTraded * order.price);
                userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                //System.out.println("Adjusting balance 2");

                aggressor.volume = aggressor.volume - order.volume;
                order.volume = 0;
                order.status = Status.FILLED;
                orders.poll();
            }
        }
    }
    public void processAsk(Deque<Order> orders,  Map<Double, Double> bidVolumes, Order aggressor) {
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
            }
            else if (order.volume > aggressor.volume) {
                double volumeTraded = aggressor.volume;
                updateVolume(bidVolumes, order.price, -volumeTraded, order.ticker, Side.BID);
                userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);

                //RecentTrades.addTrade(order.name, aggressor.name, order.ticker, order.price, volumeTraded);
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
                double volumeTraded = order.volume;
                updateVolume(bidVolumes, order.price, -volumeTraded, order.ticker, Side.BID);
                userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);
                aggressor.volume = aggressor.volume - order.volume;
                order.volume = 0;
                order.status = Status.FILLED;
                orders.poll();
            }
        }
    }
    public long bidLimitOrder(String name, Order order) {
        if (!validateBidOrder(name, order)) {
            return -1;
        }
        TreeMap<Double, Deque<Order>> asks = orderBooks.get(order.ticker).asks;
        Map<Double, Double> askVolumes = orderBooks.get(order.ticker).askVolumes;
        TreeMap<Double, Deque<Order>> bids = orderBooks.get(order.ticker).bids;
        Map<Double, Double> bidVolumes = orderBooks.get(order.ticker).bidVolumes;

        //validate order ensures that there is sufficient balance
        while (order.volume > 0 && !asks.isEmpty() && asks.firstKey() <= order.price) {
            Deque<Order> orderList = asks.get(asks.firstKey());
            processBid(orderList, askVolumes, order);
            if (orderList.isEmpty()) {
                asks.pollFirstEntry();
            }
        }
        if (order.volume > 0) {
            order.status = Status.ACTIVE;
            bids.computeIfAbsent(order.price, k -> new LinkedList<>()).add(order);
            updateVolume(bidVolumes, order.price, order.volume, order.ticker, Side.BID);
            userList.adjustUserBalance(name, -order.price * order.volume);
            orderID++;
            if (userOrders.containsKey(order.name)) {
                userOrders.get(order.name).put(orderID, order);
            }
            else {
                userOrders.put(order.name, new HashMap<>());
                userOrders.get(order.name).put(orderID, order);
            }
            return orderID;
        }
        return 0;
    }
    public long askLimitOrder(String name, Order order) {
        if (!validateAskOrder(name, order)) {
            return -1;
        }
        TreeMap<Double, Deque<Order>> asks = orderBooks.get(order.ticker).asks;
        Map<Double, Double> askVolumes = orderBooks.get(order.ticker).askVolumes;
        TreeMap<Double, Deque<Order>> bids = orderBooks.get(order.ticker).bids;
        Map<Double, Double> bidVolumes = orderBooks.get(order.ticker).bidVolumes;

        while (order.volume > 0 && !bids.isEmpty() && bids.lastKey() >= order.price) {
            Deque<Order> orderList = bids.get(bids.lastKey());
            processAsk(orderList, bidVolumes, order);
            if (orderList.isEmpty()) {
                bids.pollLastEntry();
            }
        }
        if (order.volume > 0) {
            order.status = Status.ACTIVE;
            asks.computeIfAbsent(order.price, k -> new LinkedList<>()).add(order);
            orderID++;
            updateVolume(askVolumes, order.price, order.volume, order.ticker, Side.ASK);
            userList.adjustUserTickerBalance(order.name, order.ticker, -order.volume);

            if (userOrders.containsKey(order.name)) {
                userOrders.get(order.name).put(orderID, order);
            }
            else {
                userOrders.put(order.name, new HashMap<>());
                userOrders.get(order.name).put(orderID, order);
            }
            return orderID;
        }
        return 0;
    }
    protected Map<Double, Deque<Order>> getBids(String ticker) {
        return orderBooks.get(ticker).bids;
    }
    protected Map<Double, Deque<Order>> getAsks(String ticker) {
        return orderBooks.get(ticker).asks;
    }
    public List<PriceLevel> getBidPriceLevels(String ticker) {
        Map<Double, Double> bidVolumes = orderBooks.get(ticker).bidVolumes;
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (Map.Entry<Double, Double> entry : bidVolumes.entrySet()) {
            priceLevels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }
        return priceLevels;
    }
    public List<PriceLevel> getAskPriceLevels(String ticker) {
        Map<Double, Double> askVolumes = orderBooks.get(ticker).askVolumes;
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (Map.Entry<Double, Double> entry : askVolumes.entrySet()) {
            priceLevels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }
        return priceLevels;
    }

    /*public void display() {
        System.out.println("BID ---- ");
        for (Map.Entry<Double, Deque<Order>> entry : bids.entrySet()) {
            for (Order bid : entry.getValue()) {
                System.out.println(bid.name + " " + bid.price + " " + bid.volume);
            }
        }
    }*/
    public Order getOrder(String userId, long orderId) {
        if (!userList.validUser(userId))
            return null;
        Map<Long, Order> orders = userOrders.get(userId);
        if (!orders.containsKey(orderId))
            return null;
        return orders.get(orderId);
    }
    public boolean removeOrder(String userId, long orderId) {
        if (!userList.validUser(userId))
            return false;
        Map<Long, Order> orders = userOrders.get(userId);
        if (orders != null) {
            if (orders.containsKey(orderId) && orders.get(orderId).status == Status.ACTIVE) {
                Order order = orders.get(orderId);
                orders.get(orderId).status = Status.CANCELLED;
                if (order.side == Side.BID) {
                    userList.adjustUserBalance(userId, order.price * order.volume);
                    Map<Double, Double> bidVolumes = orderBooks.get(order.ticker).bidVolumes;
                    updateVolume(bidVolumes, order.price, -order.volume, order.ticker, Side.BID);
                }
                else {
                    userList.adjustUserTickerBalance(userId, order.ticker, order.volume);
                    Map<Double, Double> askVolumes = orderBooks.get(order.ticker).askVolumes;
                    updateVolume(askVolumes, order.price, -order.volume, order.ticker, Side.ASK);
                }
                orders.remove(orderId);
                return true;
            }
        }
        return false;
    }
    public void removeAll(String userId) {
        if (!userList.validUser(userId)) {
            return;
        }

        // Retrieve user's orders
        Map<Long, Order> orders = userOrders.get(userId);
        if (orders == null || orders.isEmpty()) {
            return; // No orders to remove
        }

        boolean allRemoved = true;

        // Iterate through all orders and remove each
        for (Long orderId : new ArrayList<>(orders.keySet())) {
            boolean removed = removeOrder(userId, orderId);
        }
    }
    public double processMarketOrder(Deque<Order> orders, Map<Double, Double> volumeMap, Order aggressor, Side side) {
        double overallVolume = 0.0;
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
                continue;
            }
            // 2 Cases:
            // First Case: Finite Stack - only sell what you can own
            double aggressorVolume = aggressor.volume;
            if (side == Side.BID)
                aggressorVolume = Math.min(aggressorVolume, userList.getValidBidVolume(aggressor.name, order.ticker, order.price));
            else if (side == Side.ASK) {
                aggressorVolume = Math.min(aggressorVolume, userList.getValidAskVolume(aggressor.name, order.ticker));
            }
            if (order.volume > aggressorVolume) {
                double volumeTraded = aggressorVolume;
                double tradePrice = order.price;
                if (side == Side.BID) {
                    userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, volumeTraded * order.price);
                    //Add volume to the aggressor's ticker balance, since it is buying
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                    //Update the ask volume map and the ask if it is a bid order
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.ASK);
                }
                else {
                    userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                    //Remove volume from the aggressor's ticker balance and add to the order's balance
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                    userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);
                    //Update the bid volume map and the bid if it is a bid order
                }
                order.volume -= aggressorVolume;
                overallVolume += aggressorVolume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
                double volumeTraded = order.volume;
                double tradePrice = order.price;
                if (side == Side.BID) {
                    userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, volumeTraded * order.price);
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.ASK);
                }
                else {
                    userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                    userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.BID);
                }
                overallVolume += order.volume;
                aggressor.volume -= order.volume;
                order.volume = 0;
                order.status = Status.FILLED;
                orders.poll();
            }
        }
        return overallVolume;
    }

    public double bidMarketOrder(String name, String ticker, double volume) {
        if (!userList.validUser(name)) {
            System.out.println("Invalid");
            return 0.0;
        }
        Order marketOrder = new Order(name, ticker, 0, volume, Side.BID, Status.ACTIVE); // Price is 0 for market orders
        double volumeFilled = 0.0;
        TreeMap<Double, Deque<Order>> asks = orderBooks.get(ticker).asks;
        Map<Double, Double> askVolumes = orderBooks.get(ticker).askVolumes;
        while (marketOrder.volume > 0 && !asks.isEmpty()) {
            Deque<Order> orderList = asks.get(asks.firstKey());
            volumeFilled += processMarketOrder(orderList, askVolumes, marketOrder, Side.BID);
            if (orderList.isEmpty()) {
                asks.pollFirstEntry();
            }
        }
        if (marketOrder.volume > 0) {
            // Cancel any remaining volume
            marketOrder.status = Status.CANCELLED;
            marketOrder.volume = 0;
        } else {
            marketOrder.status = Status.FILLED;
        }
        return volumeFilled;
    }

    public double askMarketOrder(String name, String ticker, double volume) {
        if (!userList.validUser(name)) {
            System.out.println("Invalid User");
            return 0.0;
        }
        if (!userList.validAskQuantity(name, ticker, volume)) {
            System.out.println("Invalid Name");
            return 0.0;
        }
        Order marketOrder = new Order(name, ticker, 0, volume, Side.ASK, Status.ACTIVE); // Price is 0 for market orders
        double volumeFilled = 0.0;
        TreeMap<Double, Deque<Order>> bids = orderBooks.get(ticker).bids;
        Map<Double, Double> bidVolumes = orderBooks.get(ticker).bidVolumes;
        while (marketOrder.volume > 0 && !bids.isEmpty()) {
            Deque<Order> orderList = bids.get(bids.lastKey());
            volumeFilled += processMarketOrder(orderList, bidVolumes, marketOrder, Side.ASK);
            if (orderList.isEmpty()) {
                bids.pollLastEntry();
            }
        }
        if (marketOrder.volume > 0) {
            // Cancel any remaining volume
            marketOrder.status = Status.CANCELLED;
            marketOrder.volume = 0;
        } else {
            marketOrder.status = Status.FILLED;
        }
        return volumeFilled;
    }
    public String getUserDetails(String username) {
        ObjectMapper objectMapper = new ObjectMapper();

        JSONObject userListDetails = userList.getUserDetailsAsJson(username);
        if (userOrders.containsKey(username)) {
            userListDetails.put("Orders", userOrders.get(username));
        }
        return userListDetails.toString();
    }
    public void getLeaderboard(TaskFuture<List<LeaderboardEntry>> future) {
        future.setData(userList.getLeaderboard());
        future.markAsComplete();
    }
}