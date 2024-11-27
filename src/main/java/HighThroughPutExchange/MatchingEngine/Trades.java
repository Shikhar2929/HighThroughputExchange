package HighThroughPutExchange.MatchingEngine;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Trades {
    // Inner class to represent individual trades
    public static class Trade {
        private final long tradeId;
        private final String buyer;
        private final String seller;
        private final String ticker;
        private final double price;
        private final double volume;
        private final LocalDateTime timestamp;

        public Trade(long tradeId, String buyer, String seller, String ticker, double price, double volume) {
            this.tradeId = tradeId;
            this.buyer = buyer;
            this.seller = seller;
            this.ticker = ticker;
            this.price = price;
            this.volume = volume;
            this.timestamp = LocalDateTime.now();
        }

        @Override
        public String toString() {
            return "Trade{" +
                    "tradeId=" + tradeId +
                    ", buyer='" + buyer + '\'' +
                    ", seller='" + seller + '\'' +
                    ", ticker='" + ticker + '\'' +
                    ", price=" + price +
                    ", volume=" + volume +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    private final ConcurrentLinkedQueue<Trade> tradeQueue = new ConcurrentLinkedQueue<>();
    private long tradeCounter;

    public Trades() {
        this.tradeCounter = 0;
    }

    // Method to add a new trade to the queue
    public void addTrade(String buyer, String seller, String ticker, double price, double volume) {
        tradeCounter++;
        Trade trade = new Trade(tradeCounter, buyer, seller, ticker, price, volume);
        tradeQueue.add(trade); // Add trade to the queue
        System.out.println("New trade recorded: " + trade);
    }

    // Get all recent trades by popping them from the queue
    public ArrayList<Trade> getRecentTrades() {
        ArrayList<Trade> recentTrades = new ArrayList<>();
        Trade trade;
        while ((trade = tradeQueue.poll()) != null) { // Poll removes trades from the queue
            recentTrades.add(trade);
        }
        return recentTrades;
    }
}