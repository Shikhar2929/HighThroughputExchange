package HighThroughPutExchange.MatchingEngine;

public class Order {
    protected String name;
    protected String ticker;
    protected double price;
    protected double volume;
    protected Side side;
    protected Status status;
    public Order(String username, String ticker, double price, double volume, Side side, Status status) {
        this.name = username;
        this.ticker = ticker;
        this.price = price;
        this.volume = volume;
        this.side = side;
        this.status = status;
    }

    public double getPrice() {
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
                "Order{name='%s', ticker='%s', price=%.2f, volume=%.2f, side=%s, status=%s}",
                name, ticker, price, volume, side, status
        );
    }

}
