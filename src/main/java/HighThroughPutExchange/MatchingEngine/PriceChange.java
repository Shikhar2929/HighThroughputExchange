package HighThroughPutExchange.MatchingEngine;

public class PriceChange extends TradeKey{
    private final double volume;
    public PriceChange(String ticker, double price, double volume, Side side) {
        super(ticker, price, side);
        this.volume = volume;
    }
    public double getVolume() {
        return volume;
    }
}
