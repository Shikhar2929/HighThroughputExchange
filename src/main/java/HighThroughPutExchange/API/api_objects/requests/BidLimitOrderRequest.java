package HighThroughPutExchange.API.api_objects.requests;

public class BidLimitOrderRequest extends BasePrivateRequest {
    private String ticker;
    private int volume;
    private float price;

    public BidLimitOrderRequest(String username, String sessionToken, String ticker, int volume, float price) {
        super(username, sessionToken);
        this.ticker = ticker;
        this.volume = volume;
        this.price = price;
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
