package HighThroughPutExchange.MatchingEngine;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderBook {
    protected TreeMap<Double, Deque<Order>> bids = new TreeMap<>(); // Price -> Orders
    protected TreeMap<Double, Deque<Order>> asks = new TreeMap<>(); // Price -> Orders
    protected Map<Double, Double> bidVolumes = new TreeMap<>(); // Price -> Total Volume
    protected Map<Double, Double> askVolumes = new TreeMap<>(); // Price -> Total Volume
    public Map<Double, Double> getBidVolumes() {
        return bidVolumes;
    }

    public Map<Double, Double> getAskVolumes() {
        return askVolumes;
    }
    public void printOrderBook() {
        System.out.println("Bid Volumes:");
        for (Map.Entry<Double, Double> entry : bidVolumes.entrySet()) {
            System.out.println("Price: " + entry.getKey() + ", Volume: " + entry.getValue());
        }

        System.out.println("\nAsk Volumes:");
        for (Map.Entry<Double, Double> entry : askVolumes.entrySet()) {
            System.out.println("Price: " + entry.getKey() + ", Volume: " + entry.getValue());
        }
    }

}
