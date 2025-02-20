package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;
public class BotMarketOrderRequest extends BasePrivateRequest {
    @NotNull
    private String ticker;
    @NotNull
    private int volume;
    @NotNull
    private boolean isBid;

    public BotMarketOrderRequest(String username, String sessionToken, String ticker, int volume, boolean isBid) {
        super(username, sessionToken);
        this.ticker = ticker;
        this.volume = Preprocessing.botPreprocessVolume(volume);
        this.isBid = isBid;
    }
    public String getTicker() {
        return ticker;
    }
    public boolean getBid() {return isBid;}
    public void setBid(boolean bid) {this.isBid = bid;}

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = Preprocessing.botPreprocessVolume(volume);
    }
}
