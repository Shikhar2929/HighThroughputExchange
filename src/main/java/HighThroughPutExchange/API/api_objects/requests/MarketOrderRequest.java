package HighThroughPutExchange.API.api_objects.requests;

public class MarketOrderRequest extends AbstractOrderRequest {

    public MarketOrderRequest(String username,
                              String sessionToken,
                              String ticker,
                              int volume,
                              boolean isBid) {
        super(username, sessionToken, ticker, volume, isBid, Preprocessing::preprocessVolume);
    }
}
