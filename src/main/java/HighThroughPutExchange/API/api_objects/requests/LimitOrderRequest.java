package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;

public class LimitOrderRequest extends BasePrivateRequest {
    @NotNull
    private String ticker;
    @NotNull
    private int volume;
    @NotNull
    private float price;
    @NotNull
    private boolean isBid;

    public boolean getBid() {
        return isBid;
    }

    public void setBid(boolean bid) {
        isBid = bid;
    }

    public LimitOrderRequest(String username, String sessionToken, String ticker, int volume, float price, boolean isBid) {
        super(username, sessionToken);
        this.ticker = ticker;
        this.volume = volume;
        this.price = price;
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

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
