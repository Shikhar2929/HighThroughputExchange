package hte.api.dtos.requests;

public class BotMarketOrderRequest extends AbstractOrderRequest {

    public BotMarketOrderRequest(
            String username, String sessionToken, String ticker, int volume, boolean isBid) {
        super(username, sessionToken, ticker, volume, isBid, Preprocessing::botPreprocessVolume);
    }
}
