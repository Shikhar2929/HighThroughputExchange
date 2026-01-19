package HighThroughPutExchange.api.dtos.requests;

public class BotLimitOrderRequest extends AbstractLimitOrderRequest {

    public BotLimitOrderRequest(
            String username,
            String sessionToken,
            String ticker,
            int volume,
            int price,
            boolean isBid) {
        super(
                username,
                sessionToken,
                ticker,
                volume,
                price,
                isBid,
                Preprocessing::botPreprocessVolume,
                Preprocessing::preprocessPrice);
    }
}
