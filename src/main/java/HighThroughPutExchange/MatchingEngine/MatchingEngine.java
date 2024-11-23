package HighThroughPutExchange.MatchingEngine;

import java.util.*;


public class MatchingEngine {
    MatchingEngine(){}
    private TreeMap<Double, Deque<Order>> bids = new TreeMap<>();
    private TreeMap<Double, Deque<Order>> asks = new TreeMap<>();
    private Map<String, Map<Long, Order>> userOrders = new HashMap<>(); // UserName, OrderId, Order
    private Map<Double, Double> bidVolumes = new TreeMap<>(); // Price Level, Quantity
    private Map<Double, Double> askVolumes = new TreeMap<>(); // Price Level, Quantity
    private UserList userList = new UserList();
    private long orderID = 0;

    public boolean initializeUser(String username, double balance) {
        return userList.initializeUser(username, balance);
    }
    public double getUserBalance(String username) {
        return userList.getUserBalance(username);
    }
    public double getHighestBid() {
        if (bids.isEmpty()) {
            return 0.0;
        }
        return bids.lastKey();
    }
    public double getLowestAsk() {
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
        if (!userList.validUser(user) || userList.getUserBalance(user) < order.volume * order.price) {
            return false;
        }
        return true;
    }
    private boolean validateAskOrder(String user, Order order) {
        if (order.volume <= 0.0 || order.price <= 0.0 || order.status != Status.ACTIVE) {
            return false;
        }
        if (!userList.validUser(user)) {
            return false;
        }
        return true;
    }
    private void processBid(Deque<Order> orders, Order aggressor) {
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
                // MISSING SEND TRADE
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
                double volumeTraded = order.volume;
                updateVolume(askVolumes, order.price, -volumeTraded);
                userList.adjustUserBalance(order.name, volumeTraded * order.price);
                userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                // MISSING SEND TRADE INFO
                aggressor.volume = aggressor.volume - order.volume;
                order.volume = 0;
                order.status = Status.FILLED;
                orders.poll();
            }
        }
    }
    public void processAsk(Deque<Order> orders, Order aggressor) {
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.volume > aggressor.volume) {
                double volumeTraded = aggressor.volume;
                updateVolume(bidVolumes, order.price, -volumeTraded);
                userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                // MISSING SEND TRADE
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
                double volumeTraded = order.volume;
                updateVolume(bidVolumes, order.price, -volumeTraded);
                userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                // MISSING SEND TRADE INFO
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
        //validate order ensures that there is sufficient balance
        while (order.volume > 0 && !asks.isEmpty() && asks.firstKey() <= order.price) {
            Deque<Order> orderList = asks.get(asks.firstKey());
            processBid(orderList, order);
            if (orderList.isEmpty()) {
                asks.pollFirstEntry();
            }
        }
        if (order.volume > 0) {
            order.status = Status.ACTIVE;
            bids.computeIfAbsent(order.price, k -> new LinkedList<>()).add(order);
            updateVolume(bidVolumes, order.price, order.volume);
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
        while (order.volume > 0 && !bids.isEmpty() && bids.lastKey() >= order.price) {
            Deque<Order> orderList = bids.get(bids.lastKey());
            processAsk(orderList, order);
            if (orderList.isEmpty()) {
                bids.pollLastEntry();
            }
        }
        if (order.volume > 0) {
            order.status = Status.ACTIVE;
            asks.computeIfAbsent(order.price, k -> new LinkedList<>()).add(order);
            orderID++;
            updateVolume(askVolumes, order.price, order.volume);
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
    protected Map<Double, Deque<Order>> getBids() {
        return bids;
    }
    protected Map<Double, Deque<Order>> getAsks() {
        return asks;
    }
    public List<PriceLevel> getBidPriceLevels() {
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (Map.Entry<Double, Double> entry : bidVolumes.entrySet()) {
            priceLevels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }
        return priceLevels;
    }
    public List<PriceLevel> getAskPriceLevels() {
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (Map.Entry<Double, Double> entry : askVolumes.entrySet()) {
            priceLevels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }
        return priceLevels;
    }

    public void display() {
        System.out.println("BID ---- ");
        for (Map.Entry<Double, Deque<Order>> entry : bids.entrySet()) {
            for (Order bid : entry.getValue()) {
                System.out.println(bid.name + " " + bid.price + " " + bid.volume);
            }
        }
    }
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
                updateVolume(bidVolumes, order.price, -order.volume);
                orders.get(orderId).status = Status.CANCELLED;
                if (order.side == Side.BID) {
                    userList.adjustUserBalance(userId, order.price * order.volume);
                }
                return true;
            }
        }
        return false;
    }
    public double processMarketOrder(Deque<Order> orders, Order aggressor, Side side) {
        double overallVolume = 0.0;
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            double aggressorVolume = aggressor.volume;
            if (side == Side.BID)
                aggressorVolume = Math.min(aggressorVolume, userList.getValidVolume(aggressor.name, order.price));
            if (order.status == Status.CANCELLED) {
                orders.poll();
            } else if (order.volume > aggressorVolume) {
                double volumeTraded = aggressorVolume;
                double tradePrice = order.price;
                if (side == Side.BID) {
                    userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, volumeTraded * order.price);
                }
                else {
                    userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                }
                updateVolume(side == Side.BID ? askVolumes : bidVolumes, tradePrice, -volumeTraded);
                // Handle trade logic here (e.g., record or send trade info)
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
                }
                else {
                    userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                }
                updateVolume(side == Side.BID ? askVolumes : bidVolumes, tradePrice, -volumeTraded);
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

    public double bidMarketOrder(String name, double volume) {
        if (!userList.validUser(name)) {
            return 0.0;
        }
        Order marketOrder = new Order(name, 0, volume, Side.BID, Status.ACTIVE); // Price is 0 for market orders
        double volumeFilled = 0.0;
        while (marketOrder.volume > 0 && !asks.isEmpty()) {
            Deque<Order> orderList = asks.get(asks.firstKey());
            volumeFilled += processMarketOrder(orderList, marketOrder, Side.BID);
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

    public double askMarketOrder(String name, double volume) {
        if (!userList.validUser(name)) {
            return 0.0;
        }
        Order marketOrder = new Order(name, 0, volume, Side.ASK, Status.ACTIVE); // Price is 0 for market orders
        double volumeFilled = 0.0;

        while (marketOrder.volume > 0 && !bids.isEmpty()) {
            Deque<Order> orderList = bids.get(bids.lastKey());
            volumeFilled += processMarketOrder(orderList, marketOrder, Side.ASK);
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