package HighThroughPutExchange.MatchingEngine;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderBook {
    private static final Logger logger = LoggerFactory.getLogger(OrderBook.class);
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
        logger.debug("Bid Volumes:");
        for (Map.Entry<Integer, Integer> entry : bidVolumes.entrySet()) {
            logger.debug("Price: {} Volume: {}", entry.getKey(), entry.getValue());
        }

        logger.debug("Ask Volumes:");
        for (Map.Entry<Integer, Integer> entry : askVolumes.entrySet()) {
            logger.debug("Price: {} Volume: {}", entry.getKey(), entry.getValue());
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
