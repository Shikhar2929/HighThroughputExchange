package hte.common;

import java.util.ArrayList;
import java.util.List;

public class TickerData {
    private int open;
    private int high;
    private int low;
    private int close;
    private boolean firstTrade = true;
    private final List<OHLCData> historicalData = new ArrayList<>();

    public void updatePrice(int price) {
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
        open = 0;
        high = 0;
        low = 0;
        close = 0;
        firstTrade = true;
    }

    /** Checks if OHLC data is available for this ticker. */
    public boolean isDataAvailable() {
        return !firstTrade;
    }

    /** Retrieves the latest OHLC snapshot for this ticker. */
    public OHLCData getCurrentData() {
        return new OHLCData(open, high, low, close);
    }

    /** Retrieves historical OHLC data. */
    public List<OHLCData> getHistoricalData() {
        return new ArrayList<>(historicalData);
    }
}
