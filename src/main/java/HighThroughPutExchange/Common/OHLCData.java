package HighThroughPutExchange.Common;

public record OHLCData(double open, double high, double low, double close) {
    @Override
    public String toString() {
        return String.format("{Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f}", open, high, low, close);
    }
}
