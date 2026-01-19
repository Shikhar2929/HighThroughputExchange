package HighThroughPutExchange.matchingengine;

import java.time.LocalDateTime;

public class Trade {
    private final long tradeId;
    private final String buyer;
    private final String seller;
    private final String ticker;
    private final double price;
    private final double volume;
    private final LocalDateTime timestamp;

    public Trade(
            long tradeId, String buyer, String seller, String ticker, double price, double volume) {
        this.tradeId = tradeId;
        this.buyer = buyer;
        this.seller = seller;
        this.ticker = ticker;
        this.price = price;
        this.volume = volume;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public long getTradeId() {
        return tradeId;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getSeller() {
        return seller;
    }

    public String getTicker() {
        return ticker;
    }

    public double getPrice() {
        return price;
    }

    public double getVolume() {
        return volume;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Trade{"
                + "tradeId="
                + tradeId
                + ", buyer='"
                + buyer
                + '\''
                + ", seller='"
                + seller
                + '\''
                + ", ticker='"
                + ticker
                + '\''
                + ", price="
                + price
                + ", volume="
                + volume
                + ", timestamp="
                + timestamp
                + '}';
    }
}
