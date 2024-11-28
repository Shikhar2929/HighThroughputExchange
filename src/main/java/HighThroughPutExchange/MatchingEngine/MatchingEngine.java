package HighThroughPutExchange.MatchingEngine;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;


public class MatchingEngine {
    public MatchingEngine(){}
    private Map<String, OrderBook> orderBooks = new HashMap<>();
    private Map<String, Map<Long, Order>> userOrders = new HashMap<>(); // UserName, OrderId, Order
    private UserList userList = new UserList();
    private Trades trades = new Trades();
    private long orderID = 0;

    public boolean initializeUser(String username, double balance) {
        System.out.println("Initializing: " + username);
        return userList.initializeUser(username, balance);
    }
    public boolean initializeUserVolume(String username, String ticker, double volume) {
        System.out.println("Initializing: " + username + " with ticker: " + ticker);
        return userList.initializeUserQuantity(username, ticker, volume);
    }
    public double getUserBalance(String username) {
        return userList.getUserBalance(username);
    }
    public void getRecentTrades() {
        ArrayList<Trades.Trade> recentTrades = trades.getRecentTrades();
        for (Trades.Trade trade : recentTrades)
            System.out.println(trade);
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
    public boolean initializeAllTickers() {
        //Add FILE IO STUFF LATER
        initializeTicker("AAPL");
        initializeTicker("MSFT");
        initializeTicker("GOOGL");
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
    private void updateVolume(Map<Double, Double> volumeMap, double price, double delta) {
        volumeMap.put(price, volumeMap.getOrDefault(price, 0.0) + delta);
        if (volumeMap.get(price) <= 0) {
            volumeMap.remove(price);
        }
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
        if (userList.getUserBalance(user) < order.volume * order.price) {
            System.out.println("Insufficient Funds");
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
        if (!userList.validQuantity(user, order.ticker, order.volume))
            System.out.println("Insufficient Sell Funds");
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
                updateVolume(askVolumes, order.price, -volumeTraded);
                userList.adjustUserBalance(order.name, volumeTraded * order.price);
                userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                //System.out.println("Adjusting balance 1");

                trades.addTrade(aggressor.name, order.name, order.ticker, order.price, volumeTraded);
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
                double volumeTraded = order.volume;
                updateVolume(askVolumes, order.price, -volumeTraded);
                userList.adjustUserBalance(order.name, volumeTraded * order.price);
                userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                //System.out.println("Adjusting balance 2");

                trades.addTrade(aggressor.name, order.name, order.ticker, order.price, volumeTraded);
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
                updateVolume(bidVolumes, order.price, -volumeTraded);
                userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);

                trades.addTrade(order.name, aggressor.name, order.ticker, order.price, volumeTraded);
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
                double volumeTraded = order.volume;
                updateVolume(bidVolumes, order.price, -volumeTraded);
                userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);

                trades.addTrade(order.name, aggressor.name, order.ticker, order.price, volumeTraded);
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
            updateVolume(bidVolumes, order.price, order.volume);
            userList.adjustUserBalance(name, -order.price * order.volume);
            //System.out.println("Adjusting the balance");
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
            updateVolume(askVolumes, order.price, order.volume);
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
                    updateVolume(bidVolumes, order.price, -order.volume);
                }
                else {
                    userList.adjustUserTickerBalance(userId, order.ticker, order.volume);
                    Map<Double, Double> askVolumes = orderBooks.get(order.ticker).askVolumes;
                    updateVolume(askVolumes, order.price, -order.volume);

                }
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
            double aggressorVolume = aggressor.volume;
            if (side == Side.BID)
                aggressorVolume = Math.min(aggressorVolume, userList.getValidVolume(aggressor.name, order.price));
            if (order.volume > aggressorVolume) {
                double volumeTraded = aggressorVolume;
                double tradePrice = order.price;
                if (side == Side.BID) {
                    userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, volumeTraded * order.price);
                    trades.addTrade(aggressor.name, order.name, order.ticker, order.price, volumeTraded);
                    //Add volume to the aggressor's ticker balance, since it is buying
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                }
                else {
                    userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                    trades.addTrade(order.name, aggressor.name, order.ticker, order.price, volumeTraded);
                    //Remove volume from the aggressor's ticker balance and add to the order's balance
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                    userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);
                }
                updateVolume(volumeMap, tradePrice, -volumeTraded);
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
                    trades.addTrade(aggressor.name, order.name, order.ticker, order.price, volumeTraded);
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                }
                else {
                    userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                    trades.addTrade(order.name, aggressor.name, order.ticker, order.price, volumeTraded);
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                    userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);
                }
                updateVolume(volumeMap, tradePrice, -volumeTraded);
                // Handle trade logic here (e.g., record or send trade info)
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
            return 0.0;
        }
        if (userList.getUserVolume(name, ticker) < volume) {
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
}