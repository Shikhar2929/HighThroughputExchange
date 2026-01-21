package hte.matchingengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory aggregation of recent order book changes.
 *
 * <p>This is intentionally lightweight: it stores the latest volume for each (ticker, price, side)
 * key and can be drained to produce a sorted list of {@link PriceChange} entries.
 */
public class RecentTrades {

    private static final Map<TradeKey, Integer> tradeMap = new ConcurrentHashMap<>();

    public RecentTrades() {}

    @VisibleForTesting
    protected static void clearRecentTrades() {
        tradeMap.clear();
    }

    // Method to add a new trade to the queue
    public static void addTrade(String ticker, int price, int volume, Side side) {
        TradeKey tradeKey = new TradeKey(ticker, price, side);
        tradeMap.put(tradeKey, volume);
    }

    public static ArrayList<PriceChange> getRecentTrades() {
        ArrayList<PriceChange> recentTrades = new ArrayList<>();
        tradeMap.forEach(
                (key, volume) -> {
                    PriceChange priceChange =
                            new PriceChange(key.getTicker(), key.getPrice(), volume, key.getSide());
                    recentTrades.add(priceChange);
                });
        Collections.sort(recentTrades);
        tradeMap.clear();
        return recentTrades;
    }

    public static String recentTradesToJson(List<PriceChange> recentTrades) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // To make JSON output pretty
        try {
            return objectMapper.writeValueAsString(recentTrades);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing trades to JSON", e);
        }
    }

    public static String getRecentTradesAsJson() {
        ArrayList<PriceChange> recentTrades = getRecentTrades();
        return recentTradesToJson(recentTrades);
    }
}
