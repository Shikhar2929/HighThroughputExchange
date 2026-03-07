package hte.api.dtos.responses;

public class SetTickersResponse extends AbstractMessageResponse {
    private String[] tickers;

    public SetTickersResponse(String message, String[] tickers) {
        super(message);
        this.tickers = tickers;
    }

    public String[] getTickers() {
        return tickers;
    }

    public void setTickers(String[] tickers) {
        this.tickers = tickers;
    }
}
