package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;
public class MarketOrderRequest extends BasePrivateRequest {
    @NotNull
    private String ticker;
    @NotNull
    private double volume;
    @NotNull
    private boolean isBid;

    public MarketOrderRequest(String username, String sessionToken, String ticker, double volume, boolean isBid) {
        super(username, sessionToken);
        this.ticker = ticker;
        this.volume = volume;
        this.isBid = isBid;
    }
    public String getTicker() {
        return ticker;
    }
    public boolean getBid() {return isBid;}
    public boolean setBid(boolean bid) {this.isBid = bid;}

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }
}
