package HighThroughPutExchange.MatchingEngine;

import java.time.LocalDateTime;
import java.util.Objects;

public class TradeKey {
    String ticker;
    private final double price;
    private final Side side;

    public TradeKey(String ticker, double price, Side side) {
        this.ticker = ticker;
        this.price = price;
        this.side = side;
    }

    public double getPrice() {
        return price;
    }

    public String getTicker() {
        return ticker;
    }

    public Side getSide() {
        return side;
    }

    @Override
    public String toString() {
        return "PriceChange{" +
                ", ticker=" + ticker +
                ", isBid=" + side +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(side, ticker, price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeKey tradeKey = (TradeKey) o;
        return Double.compare(tradeKey.price, price) == 0 &&
                ticker.equals(tradeKey.ticker) &&
                side == tradeKey.side;
    }
}