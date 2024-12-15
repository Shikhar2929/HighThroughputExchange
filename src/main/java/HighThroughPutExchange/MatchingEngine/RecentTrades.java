package HighThroughPutExchange.MatchingEngine;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RecentTrades {

    private static final Map<TradeKey, Double> tradeMap = new ConcurrentHashMap<>();
    private static long tradeCounter = 0;

    public RecentTrades() {
    }

    // Method to add a new trade to the queue
    public static void addTrade(String ticker, double price, double volume, Side side) {
        tradeCounter++;
        TradeKey tradeKey = new TradeKey(ticker, price, side);
        tradeMap.merge(tradeKey, volume, Double::sum);
        tradeCounter++;
    }

    public static ArrayList<PriceChange> getRecentTrades() {
        ArrayList<PriceChange> recentTrades = new ArrayList<>();
        tradeMap.forEach((key, volume) -> {
            PriceChange priceChange = new PriceChange(key.getTicker(), key.getPrice(), volume, key.getSide());
            recentTrades.add(priceChange);
        });
        tradeMap.clear();
        return recentTrades;
    }
}