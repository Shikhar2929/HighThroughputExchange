package HighThroughPutExchange.MatchingEngine;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MatchingEngineTest {
    private MatchingEngine newEngine(int positionLimit, String ticker, String... users) {
        int[] userBalances = new int[users.length];
        int[] userTickerVolumes = new int[users.length];
        java.util.Arrays.fill(userBalances, 0);
        java.util.Arrays.fill(userTickerVolumes, 0);
        return newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);
    }

    private MatchingEngine newEngine(int positionLimit, String ticker, String[] users, int[] userBalances,
            int[] userTickerVolumes) {
        MatchingEngine engine;

        if (positionLimit == -1) {
            engine = new MatchingEngine();
        } else {
            engine = new MatchingEngine(positionLimit);
        }

        engine.initializeTicker(ticker);

        for (int i = 0; i < users.length; i++) {
            engine.initializeUserBalance(users[i], userBalances[i]);
            engine.initializeUserTickerVolume(users[i], ticker, userTickerVolumes[i]);
        }

        return engine;
    }

    @BeforeEach
    void resetRecentTrades() {
        // clear any trades left from previous tests
        RecentTrades.clear();
    }

    @Test
    void testBidLimitOrder_AddsBidSuccessfully() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String buyer = "buyer";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer);

        long bid_id = engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 25, Side.BID, Status.ACTIVE));
        assertTrue(bid_id > 0, "Order id should be positive for resting order");
        assertEquals(100, engine.getHighestBid(ticker));

        List<PriceLevel> bids = engine.getBidPriceLevels(ticker);
        assertEquals(1, bids.size());
        assertEquals(100.0, bids.get(0).price);
        assertEquals(25.0, bids.get(0).volume);
    }

    @Test
    void testAskLimitOrder_AddsAskSuccessfully() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String seller = "seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, seller);

        long ask_id = engine.askLimitOrder(seller, new Order(seller, ticker, 110, 30, Side.ASK, Status.ACTIVE));
        assertTrue(ask_id > 0, "Order id should be positive for resting order");
        assertEquals(110, engine.getLowestAsk(ticker));

        List<PriceLevel> asks = engine.getAskPriceLevels(ticker);
        assertEquals(1, asks.size());
        assertEquals(110.0, asks.get(0).price);
        assertEquals(30.0, asks.get(0).volume);
    }

    @Test
    void testGetHighestBidLimitOrder_AfterMultipleBids() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.bidLimitOrder(user, new Order(user, ticker, 100, 10, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 105, 5, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 103, 7, Side.BID, Status.ACTIVE));

        assertEquals(105, engine.getHighestBid(ticker));
    }

    @Test
    void testGetLowestAskLimitOrder_AfterMultipleAsks() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.askLimitOrder(user, new Order(user, ticker, 110, 10, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(user, new Order(user, ticker, 108, 5, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(user, new Order(user, ticker, 112, 7, Side.ASK, Status.ACTIVE));

        assertEquals(108, engine.getLowestAsk(ticker));
    }

    @Test
    void testMatchingBidAndAskLimitOrders() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String buyer = "buyer";
        String seller = "seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);

        long askId = engine.askLimitOrder(seller, new Order(seller, ticker, 100, 50, Side.ASK, Status.ACTIVE));
        assertTrue(askId > 0);

        long bidResultOrderId = engine.bidLimitOrder(buyer,
                new Order(buyer, ticker, 100, 50, Side.BID, Status.ACTIVE));

        // Fully matched bid returns 0 according to handler logic
        assertEquals(0, bidResultOrderId, "Fully matched bid order should return orderId 0");
        assertEquals(100, engine.getPrice(ticker));
        assertEquals(50, engine.getTickerBalance(buyer, ticker));
        assertEquals(-50, engine.getTickerBalance(seller, ticker));
    }

    @Test
    void testInsertBidLimitOrder() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.bidLimitOrder(user, new Order(user, ticker, 99, 12, Side.BID, Status.ACTIVE));
        assertEquals(99, engine.getHighestBid(ticker));
    }

    @Test
    void testInsertAskLimitOrder() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.askLimitOrder(user, new Order(user, ticker, 101, 13, Side.ASK, Status.ACTIVE));
        assertEquals(101, engine.getLowestAsk(ticker));
    }

    @Test
    void testFillLimitOrderCompletely() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String buyer = "Buyer";
        String seller = "Seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);

        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 20, Side.ASK, Status.ACTIVE));

        long bidId = engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 20, Side.BID, Status.ACTIVE));
        assertEquals(0, bidId); // fully filled
        assertTrue(engine.getAskPriceLevels(ticker).isEmpty(), "Ask levels should be empty after full fill");
        assertTrue(engine.getBidPriceLevels(ticker).isEmpty(), "Bid levels should be empty (no resting bid)");
    }

    @Test
    void testPartialFillLimitOrder() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String buyer = "Buyer";
        String seller = "Seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);

        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 100, Side.ASK, Status.ACTIVE));

        long bidId = engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 40, Side.BID, Status.ACTIVE));
        assertEquals(0, bidId); // fully fills bid, partially ask

        List<PriceLevel> asks = engine.getAskPriceLevels(ticker);
        assertEquals(1, asks.size());
        assertEquals(100.0, asks.get(0).price);
        assertEquals(60.0, asks.get(0).volume);
    }

    @Test
    void testDifferentPricesNoMatchLimitOrder() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String buyer = "Buyer";
        String seller = "Seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);

        engine.askLimitOrder(seller, new Order(seller, ticker, 105, 10, Side.ASK, Status.ACTIVE));

        long bidId = engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 10, Side.BID, Status.ACTIVE));
        assertTrue(bidId > 0, "Bid should rest (not match) because price < best ask");
        assertEquals(100, engine.getHighestBid(ticker));
        assertEquals(105, engine.getLowestAsk(ticker));
    }

    @Test
    void testCancelLimitOrderValidBid() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        long id = engine.bidLimitOrder(user, new Order(user, ticker, 100, 10, Side.BID, Status.ACTIVE));
        assertTrue(id > 0);
        assertTrue(engine.removeOrder(user, id));
        assertTrue(engine.getBidPriceLevels(ticker).isEmpty());
    }

    @Test
    void testCancelLimitOrderValidAsk() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        long id = engine.askLimitOrder(user, new Order(user, ticker, 120, 10, Side.ASK, Status.ACTIVE));
        assertTrue(id > 0);
        assertTrue(engine.removeOrder(user, id));
        assertTrue(engine.getAskPriceLevels(ticker).isEmpty());
    }

    @Test
    void testCancelNonExistentOrder() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        assertFalse(engine.removeOrder(user, 9999));
    }

    @Test
    void testBidPriceLevelsAfterAddingLimitOrder() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 7, Side.BID, Status.ACTIVE));

        List<PriceLevel> bids = engine.getBidPriceLevels(ticker);
        assertEquals(1, bids.size());
        assertEquals(12.0, bids.get(0).volume);
    }

    @Test
    void testAskPriceLevelsAfterAddingLimitOrder() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.askLimitOrder(user, new Order(user, ticker, 105, 5, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(user, new Order(user, ticker, 105, 7, Side.ASK, Status.ACTIVE));

        List<PriceLevel> asks = engine.getAskPriceLevels(ticker);
        assertEquals(1, asks.size());
        assertEquals(12.0, asks.get(0).volume);
    }

    @Test
    void testPriceLevelsAfterPartialFillLimitOrder() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String buyer = "Buyer";
        String seller = "Seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);

        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 20, Side.ASK, Status.ACTIVE));
        engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 5, Side.BID, Status.ACTIVE));

        List<PriceLevel> asks = engine.getAskPriceLevels(ticker);
        assertEquals(1, asks.size());
        assertEquals(15.0, asks.get(0).volume);
    }

    @Test
    void testPriceLevelsAfterFullFillLimitOrder() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String buyer = "Buyer";
        String seller = "Seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);

        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 10, Side.ASK, Status.ACTIVE));
        engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 10, Side.BID, Status.ACTIVE));
        assertTrue(engine.getAskPriceLevels(ticker).isEmpty());
    }

    @Test
    void testPriceLevelsWithMultipleLimitOrdersAtSamePrice() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.bidLimitOrder(user, new Order(user, ticker, 100, 3, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 4, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));

        List<PriceLevel> bids = engine.getBidPriceLevels(ticker);
        assertEquals(1, bids.size());
        assertEquals(12.0, bids.get(0).volume);
    }

    @Test
    void testRemovingLimitOrderUpdatesPriceLevels() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        long id1 = engine.bidLimitOrder(user, new Order(user, ticker, 100, 10, Side.BID, Status.ACTIVE));
        long id2 = engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        List<PriceLevel> bids = engine.getBidPriceLevels(ticker);
        assertEquals(15.0, bids.get(0).volume);
        assertTrue(engine.removeOrder(user, id1));

        bids = engine.getBidPriceLevels(ticker);
        assertEquals(5.0, bids.get(0).volume);
        assertTrue(engine.removeOrder(user, id2));
        assertTrue(engine.getBidPriceLevels(ticker).isEmpty());
    }

    @Test
    void testMarketBuyOrderFullyFilled() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String buyer = "buyer";
        String seller = "seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 20, Side.ASK, Status.ACTIVE));

        int filled = engine.bidMarketOrder(buyer, ticker, 20);
        assertEquals(20, filled);
        assertEquals(100, engine.getPrice(ticker));
    }

    @Test
    void testMarketSellOrderFullyFilled() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String seller = "seller";
        String buyer = "buyer";
        MatchingEngine engine = newEngine(positionLimit, ticker, seller, buyer);
        engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 25, Side.BID, Status.ACTIVE));

        int filled = engine.askMarketOrder(seller, ticker, 25);
        assertEquals(25, filled);
        assertEquals(100, engine.getPrice(ticker));
    }

    @Test
    void testMarketBuyOrderPartiallyFilled() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String buyer = "buyer";
        String seller = "seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 10, Side.ASK, Status.ACTIVE));

        int filled = engine.bidMarketOrder(buyer, ticker, 25);
        assertEquals(10, filled);
    }

    @Test
    void testMarketSellOrderPartiallyFilled() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String seller = "seller";
        String buyer = "buyer";
        MatchingEngine engine = newEngine(positionLimit, ticker, seller, buyer);
        engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 8, Side.BID, Status.ACTIVE));

        int filled = engine.askMarketOrder(seller, ticker, 20);
        assertEquals(8, filled);
    }

    @Test
    void testMarketOrderCancelledDueToNoLiquidity() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String buyer = "buyer";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer);

        int filled = engine.bidMarketOrder(buyer, ticker, 10);
        assertEquals(0, filled);
        assertEquals(0, engine.getPrice(ticker));
    }

    @Test
    void testUninitializedUser() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        MatchingEngine engine = newEngine(positionLimit, ticker); // no users initialized

        long id = engine.bidLimitOrder("ghost", new Order("ghost", ticker, 100, 5, Side.BID, Status.ACTIVE));
        assertEquals(-1, id);
    }
}