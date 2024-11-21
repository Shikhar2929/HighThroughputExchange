package HighThroughPutExchange.MatchingEngine;

public class Order {
    protected String name;
    protected double price;
    protected double volume;
    protected Side side;
    protected Status status;
    Order(String name, double price, double volume, Side side, Status status) {
        this.name = name;
        this.price = price;
        this.volume = volume;
        this.side = side;
        this.status = status;
    }
}
