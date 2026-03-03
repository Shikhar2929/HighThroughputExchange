package hte.api.dtos.responses;

import java.util.Map;

public class SetTickersResponse extends AbstractMessageResponse {
    private Map<String, Integer> tickers;

    public SetTickersResponse(String message, Map<String, Integer> tickers) {
        super(message);
        this.tickers = tickers;
    }

    public Map<String, Integer> getTickers() {
        return tickers;
    }

    public void setTickers(Map<String, Integer> tickers) {
        this.tickers = tickers;
    }
}
