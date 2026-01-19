package HighThroughPutExchange.matchingengine;

public class PriceChange extends TradeKey {
    private final int volume;

    public PriceChange(String ticker, int price, int volume, Side side) {
        super(ticker, price, side);
        this.volume = volume;
    }

    public int getVolume() {
        return volume;
    }

    public int compareTo(PriceChange other) {
        return super.compareTo(other);
    }
}
