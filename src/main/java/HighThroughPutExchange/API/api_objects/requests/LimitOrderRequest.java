package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;

public class LimitOrderRequest extends BasePrivateRequest {
    @NotNull
    private String ticker;
    @NotNull
    private double volume;
    @NotNull
    private double price;
    @NotNull
    private boolean isBid;

    public boolean getBid() {
        return isBid;
    }

    public void setBid(boolean bid) {
        isBid = bid;
    }

    public LimitOrderRequest(String username, String sessionToken, String ticker, double volume, double price, boolean isBid) {
        super(username, sessionToken);
        this.ticker = ticker;
        this.volume = Preprocessing.preprocessVolume(volume);
        this.price = Preprocessing.preprocessPrice(price);
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
        this.volume = Preprocessing.preprocessVolume(volume);
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = Preprocessing.preprocessPrice(price);
    }
}
