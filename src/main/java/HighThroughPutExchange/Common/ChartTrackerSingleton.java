package HighThroughPutExchange.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartTrackerSingleton {
    private static volatile ChartTrackerSingleton instance;
    private final Map<String, TickerData> tickerDataMap = new HashMap<>();

    public static ChartTrackerSingleton getInstance() {
        if (instance == null) {
            synchronized (ChartTrackerSingleton.class) {
                if (instance == null) {
                    instance = new ChartTrackerSingleton();
                }
            }
        }
        return instance;
    }

    public synchronized void updatePrice(String ticker, int price) {
        tickerDataMap.computeIfAbsent(ticker, k -> new TickerData()).updatePrice(price);
    }

    public synchronized void resetAll() {
        for (String ticker : tickerDataMap.keySet()) {
            tickerDataMap.get(ticker).reset();
        }
    }

    public synchronized Map<String, List<OHLCData>> getAllHistoricalData() {
        Map<String, List<OHLCData>> historicalData = new HashMap<>();
        for (Map.Entry<String, TickerData> entry : tickerDataMap.entrySet()) {
            historicalData.put(entry.getKey(), entry.getValue().getHistoricalData());
        }
        return historicalData;
    }

    public synchronized Map<String, OHLCData> getCurrentData() {
        Map<String, OHLCData> currentData = new HashMap<>();
        for (Map.Entry<String, TickerData> entry : tickerDataMap.entrySet()) {
            if (entry.getValue().isDataAvailable()) {
                currentData.put(entry.getKey(), entry.getValue().getCurrentData());
            }
        }
        return currentData;
    }
}
