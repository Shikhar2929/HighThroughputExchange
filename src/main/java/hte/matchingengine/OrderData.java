package hte.matchingengine;

/**
 * Accumulator for executed trade data.
 *
 * <p>{@link #price} is typically used as a linear-combination accumulator (sum(price * volume))
 * until normalized by total {@link #volume} to yield a volume-weighted average price.
 */
public class OrderData {
    public double price;
    public double volume;

    public OrderData() {
        this.price = 0.0;
        this.volume = 0.0;
    }

    public void add(OrderData newData) {
        this.price += newData.price;
        this.volume += newData.volume;
    }

    public void linearCombination(double price, double volume) {
        this.price += price * volume;
        this.volume += volume;
    }
}
