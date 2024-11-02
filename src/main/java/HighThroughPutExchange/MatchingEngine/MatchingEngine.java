package HighThroughPutExchange.MatchingEngine;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;


public class MatchingEngine {
    MatchingEngine(){}
    private TreeMap<Double, Deque<Order>> bids = new TreeMap<>();
    private TreeMap<Double, Deque<Order>> asks = new TreeMap<>();
    private Map<String, Map<Long, Order>> userOrders = new HashMap<>(); // UserName, OrderId, Order
    private long orderID = 0;

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
    private void processBid(Deque<Order> orders, Order aggressor) {
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
            }
            else if (order.volume > aggressor.volume) {
                // MISSING SEND TRADE
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
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
                // MISSING SEND TRADE
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
                // MISSING SEND TRADE INFO
                aggressor.volume = aggressor.volume - order.volume;
                order.volume = 0;
                order.status = Status.FILLED;
                orders.poll();
            }
        }
    }
    public long bidLimitOrder(String name, Order order) {
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
    public void display() {
        System.out.println("BID ---- ");
        for (Map.Entry<Double, Deque<Order>> entry : bids.entrySet()) {
            for (Order bid : entry.getValue()) {
                System.out.println(bid.name + " " + bid.price + " " + bid.volume);
            }
        }
    }
    public Order getOrder(String userId, long orderId) {
        if (!userOrders.containsKey(userId))
            return null;
        Map<Long, Order> orders = userOrders.get(userId);
        if (!orders.containsKey(orderId))
            return null;
        return orders.get(orderId);
    }
    public boolean removeOrder(String userId, long orderId) {
        Map<Long, Order> orders = userOrders.get(userId);
        if (orders != null) {
            if (orders.containsKey(orderId) && orders.get(orderId).status == Status.ACTIVE) {
                orders.get(orderId).status = Status.CANCELLED;
                return true;
            }
        }
        return false;
    }
}