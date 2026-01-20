package hte.matchingengine;

/**
 * A (ticker, price, side) change annotated with current aggregated volume.
 *
 * <p>Produced by {@link RecentTrades#getRecentTrades()} for UI/chart updates.
 */
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
