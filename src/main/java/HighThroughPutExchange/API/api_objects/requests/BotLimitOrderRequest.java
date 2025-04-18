package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;

public class BotLimitOrderRequest extends BasePrivateRequest {
    @NotNull
    private String ticker;
    @NotNull
    private int volume;
    @NotNull
    private int price;
    @NotNull
    private boolean isBid;

    public boolean getBid() {
        return isBid;
    }

    public void setBid(boolean bid) {
        isBid = bid;
    }

    public BotLimitOrderRequest(String username, String sessionToken, String ticker, int volume, int price, boolean isBid) {
        super(username, sessionToken);
        this.ticker = ticker;
        this.volume = Preprocessing.botPreprocessVolume(volume);
        this.price = Preprocessing.preprocessPrice(price);
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
        this.volume = Preprocessing.botPreprocessVolume(volume);
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = Preprocessing.preprocessPrice(price);
    }
}
