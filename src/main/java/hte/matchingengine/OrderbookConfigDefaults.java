package hte.matchingengine;

import java.util.HashMap;

/**
 * Jackson-mappable defaults section from the engine config JSON.
 *
 * <p>Expected keys include: {@code tickers}: list of tradable instruments {@code balances}: initial
 * per-ticker inventory {@code defaultBalance}: initial cash balance (finite mode)
 */
public class OrderbookConfigDefaults {
    private String[] tickers;
    private HashMap<String, Integer> balances;
    private int defaultBalance;

    public OrderbookConfigDefaults() {}

    public String[] getTickers() {
        return tickers;
    }

    public void setTickers(String[] tickers) {
        this.tickers = tickers;
    }

    public HashMap<String, Integer> getBalances() {
        return balances;
    }

    public void setBalances(HashMap<String, Integer> balances) {
        this.balances = balances;
    }

    public int getDefaultBalance() {
        return defaultBalance;
    }

    public void setDefaultBalance(int defaultBalance) {
        this.defaultBalance = defaultBalance;
    }
}
