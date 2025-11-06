package HighThroughPutExchange.API.api_objects.requests;

public class LimitOrderRequest extends AbstractLimitOrderRequest {

    public LimitOrderRequest(
            String username, String sessionToken, String ticker, int volume, int price, boolean isBid) {
        super(
                username,
                sessionToken,
                ticker,
                volume,
                price,
                isBid,
                Preprocessing::preprocessVolume,
                Preprocessing::preprocessPrice);
    }
}
