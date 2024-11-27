package HighThroughPutExchange.MatchingEngine;

public class PriceLevel {
    double price;
    double volume;

    PriceLevel(double price, double volume) {
        this.price = price;
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "PriceLevel{price=" + price + ", volume=" + volume + "}";
    }
}
