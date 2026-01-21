package hte.matchingengine;

/**
 * Basic order model used by the matching engine.
 *
 * <p>{@link #price} is meaningful for limit orders; market orders typically use price=0. {@link
 * #volume} is the remaining unfilled quantity. {@link #status} indicates whether the order can
 * still trade.
 */
public class Order {
    protected String name;
    protected String ticker;
    protected int price;
    protected int volume;
    protected Side side;
    protected Status status;

    public Order(String username, String ticker, int price, int volume, Side side, Status status) {
        this.name = username;
        this.ticker = ticker;
        this.price = price;
        this.volume = volume;
        this.side = side;
        this.status = status;
    }

    public int getPrice() {
        return price;
    }

    public Side getSide() {
        return side;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format(
                "Order{name='%s', ticker='%s', price=%d, volume=%d, side=%s, status=%s}",
                name, ticker, price, volume, side, status);
    }
}
