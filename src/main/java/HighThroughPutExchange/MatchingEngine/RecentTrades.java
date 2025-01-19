package HighThroughPutExchange.MatchingEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class RecentTrades {

    private static final Map<TradeKey, Double> tradeMap = new ConcurrentHashMap<>();
    private static long tradeCounter = 0;

    public RecentTrades() {
    }

    // Method to add a new trade to the queue
    public static void addTrade(String ticker, double price, double volume, Side side) {
        tradeCounter++;
        TradeKey tradeKey = new TradeKey(ticker, price, side);
        tradeMap.put(tradeKey, volume);
    }

    public static ArrayList<PriceChange> getRecentTrades() {
        ArrayList<PriceChange> recentTrades = new ArrayList<>();
        tradeMap.forEach((key, volume) -> {
            PriceChange priceChange = new PriceChange(key.getTicker(), key.getPrice(), volume, key.getSide());
            recentTrades.add(priceChange);
        });
        Collections.sort(recentTrades);
        tradeMap.clear();
        return recentTrades;
    }
    public static String getRecentTradesAsJson() {
        ArrayList<PriceChange> recentTrades = getRecentTrades();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // To make JSON output pretty
        try {
            return objectMapper.writeValueAsString(recentTrades);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing trades to JSON", e);
        }
    }
}