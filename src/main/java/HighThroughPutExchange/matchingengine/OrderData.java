package HighThroughPutExchange.matchingengine;

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
