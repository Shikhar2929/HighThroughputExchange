package HighThroughPutExchange.API.api_objects.Operations;

import HighThroughPutExchange.API.api_objects.requests.Preprocessing;
import jakarta.validation.constraints.NotNull;

public class LimitOrderOperation extends Operation {
    @NotNull private String ticker;
    @NotNull private int price;
    @NotNull private int volume;
    @NotNull private boolean isBid;

    public LimitOrderOperation(String ticker, int price, int volume, boolean isBid) {
        super("limit_order");
        this.ticker = ticker;
        this.price = Preprocessing.preprocessPrice(price);
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean getBid() {
        return isBid;
    }

    public void setBid(boolean isBid) {
        this.isBid = isBid;
    }
}
