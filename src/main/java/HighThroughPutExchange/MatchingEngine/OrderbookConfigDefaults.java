package HighThroughPutExchange.MatchingEngine;

import java.util.HashMap;

public class OrderbookConfigDefaults {
    private String[] tickers;
    private HashMap<String, Integer> balances;
    private int defaultBalance;

    public OrderbookConfigDefaults() {
    }

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
