package HighThroughPutExchange.MatchingEngine;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderBook {
    protected TreeMap<Integer, Deque<Order>> bids = new TreeMap<>(); // Price -> Orders
    protected TreeMap<Integer, Deque<Order>> asks = new TreeMap<>(); // Price -> Orders
    protected Map<Integer, Integer> bidVolumes = new TreeMap<>(); // Price -> Total Volume
    protected Map<Integer, Integer> askVolumes = new TreeMap<>(); // Price -> Total Volume
    private Integer currentPrice = null;
    public Map<Integer, Integer> getBidVolumes() {
        return bidVolumes;
    }

    public Map<Integer, Integer> getAskVolumes() {
        return askVolumes;
    }
    public void printOrderBook() {
        System.out.println("Bid Volumes:");
        for (Map.Entry<Integer, Integer> entry : bidVolumes.entrySet()) {
            System.out.println("Price: " + entry.getKey() + ", Volume: " + entry.getValue());
        }

        System.out.println("\nAsk Volumes:");
        for (Map.Entry<Integer, Integer> entry : askVolumes.entrySet()) {
            System.out.println("Price: " + entry.getKey() + ", Volume: " + entry.getValue());
        }
    }
    public void updatePrice(int newPrice) {
        this.currentPrice = newPrice;
    }
    public int getPrice() {
        if (currentPrice == null) {
            return 0;
        }
        return currentPrice;
    }
    public void clearOrderBook() {
        this.bidVolumes.clear();
        this.askVolumes.clear();
        this.asks.clear();
        this.bids.clear();
    }

}
