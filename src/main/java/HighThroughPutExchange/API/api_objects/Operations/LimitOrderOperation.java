package HighThroughPutExchange.API.api_objects.Operations;


import HighThroughPutExchange.API.api_objects.requests.Preprocessing;
import jakarta.validation.constraints.NotNull;

public class LimitOrderOperation extends Operation{
    @NotNull
    private String ticker;
    @NotNull
    private double price;
    @NotNull
    private double volume;
    @NotNull
    private boolean isBid;
    public LimitOrderOperation(String ticker, double price, double volume, boolean isBid) {
        super("limit_order");
        this.ticker = ticker;
        this.price = Preprocessing.preprocessPrice(price);
        this.volume = Preprocessing.preprocessVolume(volume);
        this.isBid = isBid;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
    public double getVolume() {
        return volume;
    }
    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean getBid() {
        return isBid;
    }
    public void setBid(boolean isBid) {
        this.isBid = isBid;
    }
}