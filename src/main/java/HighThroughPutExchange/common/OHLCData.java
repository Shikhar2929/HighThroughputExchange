package HighThroughPutExchange.common;

public record OHLCData(int open, int high, int low, int close) {
    @Override
    public String toString() {
        return String.format("{Open: %d, High: %d, Low: %d, Close: %d}", open, high, low, close);
    }
}
