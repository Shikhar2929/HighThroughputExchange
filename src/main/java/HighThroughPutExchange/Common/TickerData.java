package HighThroughPutExchange.Common;

import java.util.ArrayList;
import java.util.List;

public class TickerData {
    private double open;
    private double high;
    private double low;
    private double close;
    private boolean firstTrade = true;
    private final List<OHLCData> historicalData = new ArrayList<>();

    public void updatePrice(double price) {
        if (firstTrade) {
            open = price;
            high = price;
            low = price;
            firstTrade = false;
        }
        high = Math.max(high, price);
        low = Math.min(low, price);
        close = price;
    }

    public void reset() {
        if (!firstTrade) {
            historicalData.add(new OHLCData(open, high, low, close));
        }
        open = 0.0;
        high = 0.0;
        low = Double.MAX_VALUE;
        close = 0.0;
        firstTrade = true;
    }

    /**
     * Checks if OHLC data is available for this ticker.
     */
    public boolean isDataAvailable() {
        return !firstTrade;
    }

    /**
     * Retrieves the latest OHLC snapshot for this ticker.
     */
    public OHLCData getCurrentData() {
        return new OHLCData(open, high, low, close);
    }

    /**
     * Retrieves historical OHLC data.
     */
    public List<OHLCData> getHistoricalData() {
        return new ArrayList<>(historicalData);
    }
}

