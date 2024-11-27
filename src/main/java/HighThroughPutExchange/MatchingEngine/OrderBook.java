package HighThroughPutExchange.MatchingEngine;

import java.util.*;

public class OrderBook {
    protected TreeMap<Double, Deque<Order>> bids = new TreeMap<>(); // Price -> Orders
    protected TreeMap<Double, Deque<Order>> asks = new TreeMap<>(); // Price -> Orders
    protected Map<Double, Double> bidVolumes = new TreeMap<>(); // Price -> Total Volume
    protected Map<Double, Double> askVolumes = new TreeMap<>(); // Price -> Total Volume

}
