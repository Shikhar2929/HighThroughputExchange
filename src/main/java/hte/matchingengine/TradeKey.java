package hte.matchingengine;

import java.util.Objects;

public class TradeKey implements Comparable<TradeKey> {
    String ticker;
    private final int price;
    private final Side side;

    public TradeKey(String ticker, int price, Side side) {
        this.ticker = ticker;
        this.price = price;
        this.side = side;
    }

    public int getPrice() {
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
        return "PriceChange{" + ", ticker=" + ticker + ", isBid=" + side + '}';
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
        return Integer.compare(tradeKey.price, price) == 0
                && ticker.equals(tradeKey.ticker)
                && side == tradeKey.side;
    }

    @Override
    public int compareTo(TradeKey other) {
        int sideCompare = this.side.compareTo(other.side);
        if (sideCompare != 0) {
            return sideCompare;
        }
        if (this.side == Side.BID) {
            return Integer.compare(other.price, this.price);
        } else {
            return Integer.compare(this.price, other.price);
        }
    }
}
