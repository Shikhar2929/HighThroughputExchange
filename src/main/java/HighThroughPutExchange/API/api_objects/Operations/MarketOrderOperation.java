package HighThroughPutExchange.API.api_objects.Operations;

import HighThroughPutExchange.API.api_objects.requests.Preprocessing;
import jakarta.validation.constraints.NotNull;

public class MarketOrderOperation extends Operation {
    @NotNull private String ticker;
    @NotNull private int volume;
    @NotNull private boolean isBid;

    public MarketOrderOperation(String type, String ticker, int volume, boolean isBid) {
        super("market_order");
        this.ticker = ticker;
        this.volume = Preprocessing.botPreprocessVolume(volume);
        this.isBid = isBid;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public boolean getBid() {
        return isBid;
    }

    public void setBid(boolean isBid) {
        this.isBid = isBid;
    }
}
