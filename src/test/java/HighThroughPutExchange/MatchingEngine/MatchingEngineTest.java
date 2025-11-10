package HighThroughPutExchange.MatchingEngine;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;

public class MatchingEngineTest {
    private MatchingEngine newEngine(int positionLimit, String ticker, String... users) {
        MatchingEngine engine = new MatchingEngine(positionLimit);
        engine.initializeTicker(ticker);
        for (String u : users) {
            engine.initializeUserBalance(u, 0);
            engine.initializeUserTickerVolume(u, ticker, 0);
        }
        return engine;
    }

    @Test
    void testBidLimitOrder_AddsBidSuccessfully() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "buyer";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer);
        long id = engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 25, Side.BID, Status.ACTIVE));
        assertTrue(id > 0, "Order id should be positive for resting order");
        assertEquals(100, engine.getHighestBid(ticker));
        List<PriceLevel> bids = engine.getBidPriceLevels(ticker);
        assertEquals(1, bids.size());
        assertEquals(100.0, bids.get(0).price);
        assertEquals(25.0, bids.get(0).volume);
    }

    @Test
    void testAskLimitOrder_AddsAskSuccessfully() {
        int positionLimit = 1000;
        String ticker = "A";
        String seller = "seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, seller);
        long id = engine.askLimitOrder(seller, new Order(seller, ticker, 110, 30, Side.ASK, Status.ACTIVE));
        assertTrue(id > 0);
        assertEquals(110, engine.getLowestAsk(ticker));
        List<PriceLevel> asks = engine.getAskPriceLevels(ticker);
        assertEquals(1, asks.size());
        assertEquals(110.0, asks.get(0).price);
        assertEquals(30.0, asks.get(0).volume);
    }

    @Test
    void testGetHighestBid_AfterMultipleBids() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 10, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 105, 5, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 103, 7, Side.BID, Status.ACTIVE));
        assertEquals(105, engine.getHighestBid(ticker));
    }

    @Test
    void testGetLowestAsk_AfterMultipleAsks() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        engine.askLimitOrder(user, new Order(user, ticker, 110, 10, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(user, new Order(user, ticker, 108, 5, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(user, new Order(user, ticker, 112, 7, Side.ASK, Status.ACTIVE));
        assertEquals(108, engine.getLowestAsk(ticker));
    }

    @Test
    void testMatchingBidAndAskOrders() {
        int positionLimit = 1000;
        String ticker = "A";
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
    void testInsertBid() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        engine.bidLimitOrder(user, new Order(user, ticker, 99, 12, Side.BID, Status.ACTIVE));
        assertEquals(99, engine.getHighestBid(ticker));
    }

    @Test
    void testInsertAsk() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        engine.askLimitOrder(user, new Order(user, ticker, 101, 13, Side.ASK, Status.ACTIVE));
        assertEquals(101, engine.getLowestAsk(ticker));
    }

    @Test
    void testFillOrderCompletely() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "b";
        String seller = "s";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 20, Side.ASK, Status.ACTIVE));
        long bidId = engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 20, Side.BID, Status.ACTIVE));
        assertEquals(0, bidId); // fully filled
        assertTrue(engine.getAskPriceLevels(ticker).isEmpty(), "Ask levels should be empty after full fill");
        assertTrue(engine.getBidPriceLevels(ticker).isEmpty(), "Bid levels should be empty (no resting bid)");
    }

    @Test
    void testPartialFill() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "b";
        String seller = "s";
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
    void testDifferentPricesNoMatch() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "b";
        String seller = "s";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.askLimitOrder(seller, new Order(seller, ticker, 105, 10, Side.ASK, Status.ACTIVE));
        long bidId = engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 10, Side.BID, Status.ACTIVE));
        assertTrue(bidId > 0, "Bid should rest (not match) because price < best ask");
        assertEquals(100, engine.getHighestBid(ticker));
        assertEquals(105, engine.getLowestAsk(ticker));
    }

    @Test
    void testCancelOrderValidBid() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        long id = engine.bidLimitOrder(user, new Order(user, ticker, 100, 10, Side.BID, Status.ACTIVE));
        assertTrue(id > 0);
        assertTrue(engine.removeOrder(user, id));
        assertTrue(engine.getBidPriceLevels(ticker).isEmpty());
    }

    @Test
    void testCancelOrderValidAsk() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        long id = engine.askLimitOrder(user, new Order(user, ticker, 120, 10, Side.ASK, Status.ACTIVE));
        assertTrue(id > 0);
        assertTrue(engine.removeOrder(user, id));
        assertTrue(engine.getAskPriceLevels(ticker).isEmpty());
    }

    @Test
    void testCancelOrderNonExistent() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        assertFalse(engine.removeOrder(user, 9999));
    }

    @Test
    void testBidPriceLevelsAfterAddingLimitOrder() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
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
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        engine.askLimitOrder(user, new Order(user, ticker, 105, 5, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(user, new Order(user, ticker, 105, 7, Side.ASK, Status.ACTIVE));
        List<PriceLevel> asks = engine.getAskPriceLevels(ticker);
        assertEquals(1, asks.size());
        assertEquals(12.0, asks.get(0).volume);
    }

    @Test
    void testPriceLevelsAfterPartialFill() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "b";
        String seller = "s";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 20, Side.ASK, Status.ACTIVE));
        engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 5, Side.BID, Status.ACTIVE));
        List<PriceLevel> asks = engine.getAskPriceLevels(ticker);
        assertEquals(1, asks.size());
        assertEquals(15.0, asks.get(0).volume);
    }

    @Test
    void testPriceLevelsAfterFullFill() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "b";
        String seller = "s";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 10, Side.ASK, Status.ACTIVE));
        engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 10, Side.BID, Status.ACTIVE));
        assertTrue(engine.getAskPriceLevels(ticker).isEmpty());
    }

    @Test
    void testPriceLevelsWithMultipleOrdersAtSamePrice() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 3, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 4, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        List<PriceLevel> bids = engine.getBidPriceLevels(ticker);
        assertEquals(1, bids.size());
        assertEquals(12.0, bids.get(0).volume);
    }

    @Test
    void testRemovingOrderUpdatesPriceLevels() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
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
        String ticker = "A";
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
        String ticker = "A";
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
        String ticker = "A";
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
        String ticker = "A";
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
        String ticker = "A";
        String buyer = "buyer";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer);
        int filled = engine.bidMarketOrder(buyer, ticker, 10);
        assertEquals(0, filled);
        assertEquals(0, engine.getPrice(ticker));
    }

    @Test
    void testUninitializedUser() {
        int positionLimit = 1000;
        String ticker = "A";
        MatchingEngine engine = newEngine(positionLimit, ticker); // no users initialized
        long id = engine.bidLimitOrder("ghost", new Order("ghost", ticker, 100, 5, Side.BID, Status.ACTIVE));
        assertEquals(-1, id);
    }

    @Test
    void testBidLimitOrderBalanceUpdates() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "buyer";
        String seller = "seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 10, Side.ASK, Status.ACTIVE));
        engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 6, Side.BID, Status.ACTIVE));
        assertEquals(6, engine.getTickerBalance(buyer, ticker));
        assertEquals(-6, engine.getTickerBalance(seller, ticker));
    }

    @Test
    void testAskLimitOrderBalanceUpdates() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "buyer";
        String seller = "seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 10, Side.BID, Status.ACTIVE));
        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 4, Side.ASK, Status.ACTIVE));
        assertEquals(4, engine.getTickerBalance(buyer, ticker));
        assertEquals(-4, engine.getTickerBalance(seller, ticker));
    }

    @Test
    void testBidMarketOrderBalanceUpdates() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "buyer";
        String seller = "seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 10, Side.ASK, Status.ACTIVE));
        engine.bidMarketOrder(buyer, ticker, 7);
        assertEquals(7, engine.getTickerBalance(buyer, ticker));
        assertEquals(-7, engine.getTickerBalance(seller, ticker));
    }

    @Test
    void testRemoveOrderRestoresAllProperties() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        long id = engine.bidLimitOrder(user, new Order(user, ticker, 100, 9, Side.BID, Status.ACTIVE));
        assertTrue(id > 0);
        assertTrue(engine.removeOrder(user, id));
        assertTrue(engine.getBidPriceLevels(ticker).isEmpty());
    }

    @Test
    void testRemoveOrderForAskRestoresAllProperties() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        long id = engine.askLimitOrder(user, new Order(user, ticker, 150, 9, Side.ASK, Status.ACTIVE));
        assertTrue(id > 0);
        assertTrue(engine.removeOrder(user, id));
        assertTrue(engine.getAskPriceLevels(ticker).isEmpty());
    }

    @Test
    void testRemoveAllRestoresAllProperties() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        long id1 = engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        long id2 = engine.askLimitOrder(user, new Order(user, ticker, 150, 7, Side.ASK, Status.ACTIVE));
        assertTrue(id1 > 0 && id2 > 0);
        engine.removeAll(user);
        assertTrue(engine.getBidPriceLevels(ticker).isEmpty());
        assertTrue(engine.getAskPriceLevels(ticker).isEmpty());
    }

    @Test
    void initializeAllTickers() {
        MatchingEngine engine = new MatchingEngine(1000);
        assertTrue(engine.initializeAllTickers());
        // config.json has tickers A,B,C
        engine.initializeUserBalance("u", 0);
        engine.initializeUserTickerVolume("u", "A", 0);
        engine.initializeUserTickerVolume("u", "B", 0);
        engine.initializeUserTickerVolume("u", "C", 0);
        long id = engine.bidLimitOrder("u", new Order("u", "A", 100, 1, Side.BID, Status.ACTIVE));
        assertTrue(id > 0);
    }

    @Test
    void initializeAllUsers() {
        MatchingEngine engine = new MatchingEngine(1000);
        engine.initializeTicker("A");
        engine.initializeTicker("B");
        engine.initializeTicker("C");
        assertTrue(engine.initializeUser("alice"));
        assertTrue(engine.initializeUser("bob"));
        assertEquals(0, engine.getUserBalance("alice"));
        assertEquals(0, engine.getUserBalance("bob"));
    }

    @Test
    void testAddTradeAndCheckVolumeSummation() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 7, Side.BID, Status.ACTIVE));
        var trades = engine.getRecentTrades();
        assertFalse(trades.isEmpty());
    }

    @Test
    void testTradeLoggingForDifferentPrices() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 105, 4, Side.BID, Status.ACTIVE));
        var trades = engine.getRecentTrades();
        assertTrue(trades.size() >= 2);
    }

    @Test
    void testTradeClearingPostRetrieval() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        var first = engine.getRecentTrades();
        assertFalse(first.isEmpty());
        var second = engine.getRecentTrades();
        assertTrue(second.isEmpty(), "After retrieval trades map should be cleared");
    }

    @Test
    void testLimitOrderTradeLogging() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "b";
        String seller = "s";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 5, Side.ASK, Status.ACTIVE));
        engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 5, Side.BID, Status.ACTIVE));
        @SuppressWarnings("unchecked")
        var trades = (List<PriceChange>) engine.getRecentTrades();
        boolean hasPrice100 = false;
        for (PriceChange pc : trades) {
            if (pc.getPrice() == 100) {
                hasPrice100 = true;
                break;
            }
        }
        assertTrue(hasPrice100);
    }

    @Test
    void testMarketOrderTradeLogging() {
        int positionLimit = 1000;
        String ticker = "A";
        String buyer = "b";
        String seller = "s";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);
        engine.askLimitOrder(seller, new Order(seller, ticker, 100, 5, Side.ASK, Status.ACTIVE));
        engine.bidMarketOrder(buyer, ticker, 3);
        var trades = engine.getRecentTrades();
        assertFalse(trades.isEmpty());
    }

    @Test
    void testCancelOrderTradeLogging() {
        int positionLimit = 1000;
        String ticker = "A";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        long id = engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        assertTrue(engine.removeOrder(user, id));
        @SuppressWarnings("unchecked")
        var trades = (List<PriceChange>) engine.getRecentTrades();
        boolean hasPrice100 = false;
        for (PriceChange pc : trades) {
            if (pc.getPrice() == 100) {
                hasPrice100 = true;
                break;
            }
        }
        assertTrue(hasPrice100);
    }

    @Test
    void infiniteTest1() {
        int positionLimit = 100;
        String ticker = "AAPL";

        // Seller with regular balance
        String seller1 = "Seller1";
        String seller2 = "Seller2";

        // Trader with infinite balance
        String buyer = "InfiniteTrader";
        MatchingEngine engine = newEngine(positionLimit, ticker, seller1, seller2, buyer);

        // Two sellers place sell orders
        engine.askLimitOrder(seller1, new Order(seller1, "AAPL", 150, 60, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(seller2, new Order(seller2, "AAPL", 150, 40, Side.ASK, Status.ACTIVE));
        long orderId = engine.askLimitOrder(seller1, new Order(seller1, "AAPL", 150, 40, Side.ASK, Status.ACTIVE));
        assertEquals(engine.getUserBalance(seller1), 0); // Shouldn't get more balance before placing the trade
        assertEquals(0, engine.getTickerBalance(seller1, "AAPL"));
        // Buyer places two market orders
        double firstFill = engine.bidMarketOrder(buyer, "AAPL", 60);
        double secondFill = engine.bidMarketOrder(buyer, "AAPL", 60);
        System.out.println(firstFill);
        System.out.println(secondFill);
        // Ensure only up to the position limit was filled
        assertEquals(100, firstFill + secondFill, "Trader should not be able to own more than the position limit");

        // Ensure the trader's position does not exceed the limit
        assertEquals(100, engine.getTickerBalance(buyer, "AAPL"),
                "Trader's position should not exceed the position limit");
        assertEquals(-15000, engine.getUserBalance(buyer));
        assertEquals(9000, engine.getUserBalance(seller1));
        assertEquals(6000, engine.getUserBalance(seller2));
        assertEquals(0, engine.getUserBalance(buyer) + engine.getUserBalance(seller1) + engine.getUserBalance(seller2));
        assertEquals(0, engine.getTickerBalance(buyer, ticker) + engine.getTickerBalance(seller1, ticker)
                + engine.getTickerBalance(seller2, ticker));
        engine.removeOrder(seller1, orderId);
        assertEquals(0, engine.getTickerBalance(buyer, ticker) + engine.getTickerBalance(seller1, ticker)
                + engine.getTickerBalance(seller2, ticker));
    }

    @Test
    public void infiniteTestTradingWithSelf() {
        int positionLimit = 1000;
        String user = "TraderZ";
        String ticker = "A";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        // Try to buy up to position limit
        System.out.println("This Far");
        Order buyOrder = new Order(user, ticker, 150, 50, Side.BID, Status.ACTIVE); // Buy additional 50 to reach 100
        long orderId = engine.bidLimitOrder(user, buyOrder);
        System.out.println(orderId);
        assertTrue(orderId > 0, "Should allow buying within position limit");
        assertEquals(0, engine.getTickerBalance(user, ticker));
        assertEquals(0, engine.getUserBalance(user));
        // Try to short up to position limit
        Order sellOrder = new Order(user, ticker, 150, 50, Side.ASK, Status.ACTIVE); // Short additional 50 to reach
                                                                                     // -100
        orderId = engine.askLimitOrder(user, sellOrder);
        assertEquals(0, orderId, "Should be completely filled");
        assertEquals(0, engine.getTickerBalance(user, ticker));
        assertEquals(0, engine.getUserBalance(user));
    }

    @Test
    public void infiniteTestRemove() {
        int positionLimit = 1000;
        String user1 = "A";
        String user2 = "B";
        String ticker = "Spades";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);
        long orderId = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1000, Side.BID, Status.ACTIVE));
        assertEquals(0, engine.getUserBalance(user1));
        assertEquals(0, engine.getTickerBalance(user1, ticker));
        assertEquals(0, engine.getPrice(ticker), "No Matching yet, should be 0");

        engine.removeOrder(user1, orderId);
        assertEquals(0, engine.getUserBalance(user1));
        assertEquals(0, engine.getTickerBalance(user1, ticker));
        engine.askLimitOrder(user2, new Order(user2, ticker, 100, 100, Side.ASK, Status.ACTIVE));
        assertEquals(0, engine.getPrice(ticker), "No Matching yet, should be 0");
        assertEquals(0, engine.getUserBalance(user2));
        assertEquals(0, engine.getTickerBalance(user2, ticker));

        engine.bidLimitOrder(user1, new Order(user1, ticker, 101, 100, Side.BID, Status.ACTIVE));
        // engine.bidMarketOrder(user1, ticker, 100);
        assertEquals(-100 * 100, engine.getUserBalance(user1));
        assertEquals(100 * 100, engine.getUserBalance(user2));
        assertEquals(100, engine.getTickerBalance(user1, ticker));
        assertEquals(-100, engine.getTickerBalance(user2, ticker));
        assertEquals(100, engine.getPrice(ticker), "Matched, should be 100");
    }

    @Test
    public void exceedPositionLimit() {
        int positionLimit = 1000;
        String user1 = "A";
        String user2 = "B";
        String ticker = "Spades";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);
        long orderId = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1001, Side.BID, Status.ACTIVE));
        assertEquals(-1, orderId);
        orderId = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1000, Side.BID, Status.ACTIVE));
        assertEquals(1, orderId);
        assertEquals(0, engine.getUserBalance(user1));
        assertEquals(0, engine.getTickerBalance(user1, ticker));
        assertEquals(0, engine.getPrice(ticker), "No Matching yet, should be 0");
        double volumeFilled = engine.askMarketOrder(user2, ticker, 1001);
        assertEquals(0, volumeFilled);
        assertEquals(0, engine.getPrice(ticker), "No Matching yet, should be 0");

        volumeFilled = engine.askMarketOrder(user2, ticker, 999);
        assertEquals(999, volumeFilled);
        assertEquals(999 * 100, engine.getUserBalance(user2));
        assertEquals(999, engine.getTickerBalance(user1, ticker));
        assertEquals(-999, engine.getTickerBalance(user2, ticker));
        assertEquals(100, engine.getPrice(ticker), "Matched, should be 100");
    }

    @Test
    public void infiniteTestMarketOrder() {
        int positionLimit = 1000;
        String user1 = "A";
        String user2 = "B";
        String ticker = "Spades";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);
        long orderId = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1000, Side.BID, Status.ACTIVE));
        assertEquals(1, orderId);
        assertEquals(0, engine.getUserBalance(user1));
        assertEquals(0, engine.getPrice(ticker), "No Matching yet, should be 0");

        double volumeFilled = engine.askMarketOrder(user1, ticker, 10);
        assertEquals(0, engine.getUserBalance(user1));
        assertEquals(0, engine.getTickerBalance(user1, ticker));
        assertEquals(100, engine.getPrice(ticker), "Matched, should be 100");

        volumeFilled = engine.askMarketOrder(user2, ticker, 900);
        assertEquals(-900 * 100, engine.getUserBalance(user1));
        assertEquals(900 * 100, engine.getUserBalance(user2));
        assertEquals(900, engine.getTickerBalance(user1, ticker));
        assertEquals(-900, engine.getTickerBalance(user2, ticker));

        engine.removeOrder(user1, orderId);
        assertEquals(-100 * 900, engine.getUserBalance(user1));

        orderId = engine.askLimitOrder(user1, new Order(user1, ticker, 105, 100, Side.ASK, Status.ACTIVE));
        volumeFilled = engine.bidMarketOrder(user2, ticker, 300);
        assertEquals(100, volumeFilled);
        assertEquals(0, engine.getUserBalance(user1) + engine.getUserBalance(user2));
        assertEquals(-800, engine.getTickerBalance(user2, ticker));
        assertEquals(800, engine.getTickerBalance(user1, ticker));
        assertEquals(105, engine.getPrice(ticker), "Matched, should be 105");
    }

    @Test
    public void positionLimitEdgeCases() {
        int positionLimit = 1000;
        String ticker = "A";
        String user1 = "u1";
        String user2 = "u2";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);

        long orderId = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 100, Side.ASK, Status.ACTIVE));
        assertNotEquals(-1, orderId);
        assertEquals(0, engine.getUserBalance(user1));
        assertEquals(0, engine.getTickerBalance(user1, ticker));
        double volumeFilled = engine.askMarketOrder(user2, ticker, 100);
        assertEquals(100, volumeFilled);
        assertEquals(10000, engine.getUserBalance(user2));
        assertEquals(-10000, engine.getUserBalance(user1));
        assertEquals(100, engine.getTickerBalance(user1, ticker));
        assertEquals(-100, engine.getTickerBalance(user2, ticker));

        orderId = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 900, Side.BID, Status.ACTIVE));
        assertNotEquals(-1, orderId);
        assertEquals(-10000, engine.getUserBalance(user1));
        assertEquals(100, engine.getTickerBalance(user1, ticker));
        long orderId2 = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1, Side.BID, Status.ACTIVE));
        assertEquals(-1, orderId2, "Exceeding Position Limit on Bid Size");
        orderId2 = engine.askLimitOrder(user1, new Order(user1, ticker, 150, 1101, Side.BID, Status.ACTIVE));
        assertEquals(-1, orderId2);
        orderId2 = engine.askLimitOrder(user1, new Order(user1, ticker, 150, 1100, Side.BID, Status.ACTIVE));
        assertNotEquals(-1, orderId2);
        orderId2 = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1, Side.BID, Status.ACTIVE));
        assertEquals(-1, orderId2, "Exceeding Position Limit on Bid Size");

        boolean status = engine.removeOrder(user1, orderId);
        assertTrue(status);
        orderId2 = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1, Side.BID, Status.ACTIVE));
        assertNotEquals(-1, orderId2, "Order should now go through");
    }

    @Test
    public void testBots() {
        String ticker = "A";
        int positionLimit = 100;
        String user1 = "u1";
        String user2 = "u2";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);

        String bot1 = "b1";
        engine.initializeBot(bot1);
        long orderId = engine.bidLimitOrder(bot1, new Order(bot1, ticker, 10, 1000, Side.BID, Status.ACTIVE));
        assertNotEquals(-1, orderId);
        orderId = engine.askLimitOrder(bot1, new Order(bot1, ticker, 15, 1000, Side.ASK, Status.ACTIVE));
        assertNotEquals(-1, orderId);
    }

    @Test
    public void testPrice() {
        String ticker = "A";
        int positionLimit = 100;
        String user1 = "u1";
        String user2 = "u2";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);

        Map<String, Object> message = engine.askLimitOrderHandler(user1,
                new Order(user1, ticker, 15, 80, Side.ASK, Status.ACTIVE));
        assertEquals(0, (double) message.get("price"));
        assertEquals(0, (int) message.get("volumeFilled"));
        message = engine.bidLimitOrderHandler(user2, new Order(user2, ticker, 16, 10, Side.BID, Status.ACTIVE));
        assertEquals(15, (double) message.get("price"));
        assertEquals(10, (int) message.get("volumeFilled"));
        message = engine.askLimitOrderHandler(user1, new Order(user1, ticker, 14, 5, Side.ASK, Status.ACTIVE));
        assertEquals(0, (double) message.get("price"));
        assertEquals(0, (int) message.get("volumeFilled"));
        message = engine.bidMarketOrderHandler(user2, ticker, 15);
        assertEquals((14.0 * 5 + 10 * 15) / 15, (double) message.get("price"));
        assertEquals(15, message.get("volumeFilled"));
    }

    @Test
    public void testAskMarketOrderPrice() {
        String ticker = "B";
        int positionLimit = 100;
        String user1 = "u1";
        String user2 = "u2";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);

        engine.bidLimitOrderHandler(user2, new Order(user2, ticker, 20, 10, Side.BID, Status.ACTIVE));
        engine.bidLimitOrderHandler(user2, new Order(user2, ticker, 18, 5, Side.BID, Status.ACTIVE));

        Map<String, Object> message = engine.askMarketOrderHandler(user1, ticker, 12);
        assertEquals((10.0 * 20 + 2 * 18) / 12, (double) message.get("price"));
        assertEquals(12, message.get("volumeFilled"));
    }

    @Test
    public void testBidLimitOrderPrice() {
        String ticker = "C";
        int positionLimit = 100;
        String user1 = "u1";
        String user2 = "u2";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);

        engine.askLimitOrder(user1, new Order(user1, ticker, 25, 8, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(user1, new Order(user1, ticker, 28, 2, Side.ASK, Status.ACTIVE));
        Map<String, Object> message = engine.bidLimitOrderHandler(user2,
                new Order(user2, ticker, 30, 12, Side.BID, Status.ACTIVE));
        assertEquals((25.0 * 8 + 28.0 * 2) / 10.0, (double) message.get("price"));
        assertEquals(10, message.get("volumeFilled"));
    }

    @Test
    public void testAskLimitOrderPrice() {
        String ticker = "D";
        int positionLimit = 100;
        String user1 = "u1";
        String user2 = "u2";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);

        engine.bidLimitOrder(user2, new Order(user2, ticker, 30, 10, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user2, new Order(user2, ticker, 29, 3, Side.BID, Status.ACTIVE));
        Map<String, Object> message = engine.askLimitOrderHandler(user1,
                new Order(user1, ticker, 27, 11, Side.ASK, Status.ACTIVE));
        double price = (30.0 * 10 + 29 * 1) / 11;
        assertEquals(price, (double) message.get("price"));
        assertEquals(11, message.get("volumeFilled"));
        assertEquals(-(30 * 10 + 29 * 1), engine.getUserBalance(user2));
        assertEquals((30 * 10 + 29 * 1), engine.getUserBalance(user1));
    }

    // Place Bid LimitOrder and its filled askLimitOrder
    @Test
    public void infiniteBrokenTest() {
        String ticker = "D";
        int positionLimit = 100;
        String user1 = "u1";
        String user2 = "u2";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);

        engine.bidLimitOrder(user2, new Order(user2, ticker, 101, 10, Side.BID, Status.ACTIVE));
        engine.askLimitOrder(user1, new Order(user1, ticker, 101, 10, Side.ASK, Status.ACTIVE));
        assertEquals(101 * 10, engine.getUserBalance(user1));
        assertEquals(-101 * 10, engine.getUserBalance(user2));
        assertEquals(-10, engine.getTickerBalance(user1, ticker));
        assertEquals(10, engine.getTickerBalance(user2, ticker));
    }

    @Test
    public void testMarketOrdersRigorously() {
        String ticker = "D";
        int positionLimit = 1000;
        String user1 = "u1";
        String user2 = "u2";
        String user3 = "u3";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2, user3);
        long orderId = engine.bidLimitOrder(user1, new Order(user1, ticker, 200, 1000, Side.BID, Status.ACTIVE));
        assertEquals(1, orderId);
        orderId = engine.askLimitOrder(user1, new Order(user1, ticker, 250, 1000, Side.BID, Status.ACTIVE));
        assertEquals(2, orderId);

        // User 2 will buy a market order and then user 3 will provide multiple levels
        // to sell it at
        double volumeFilled = engine.bidMarketOrder(user2, ticker, 1000);
        assertEquals(1000, volumeFilled, "Market buy should fill full available ask volume");
        assertEquals(1000, engine.getTickerBalance(user2, ticker));
        assertEquals(-1000, engine.getTickerBalance(user1, ticker));
        assertEquals(-1000 * 250, engine.getUserBalance(user2));
        assertEquals(1000 * 250, engine.getUserBalance(user1));
        assertEquals(250, engine.getPrice(ticker));
        assertEquals(0, engine.getPnl(user1));
        assertEquals(0, engine.getPnl(user2));

        // User 3 will now insert orders
        orderId = engine.bidLimitOrder(user3, new Order(user3, ticker, 235, 200, Side.BID, Status.ACTIVE));
        assertEquals(3, orderId);
        orderId = engine.bidLimitOrder(user3, new Order(user3, ticker, 240, 200, Side.BID, Status.ACTIVE));
        assertEquals(4, orderId);

        engine.askMarketOrder(user2, ticker, 400);
        assertEquals(235, engine.getPrice(ticker));
        assertEquals(400, engine.getTickerBalance(user3, ticker));
        assertEquals(-235 * 200 - 240 * 200, engine.getUserBalance(user3));
        assertEquals(-5 * 200, engine.getPnl(user3));
        assertEquals(15 * 1000, engine.getPnl(user1));
        assertEquals(-14 * 1000, engine.getPnl(user2));
    }

    @Test
    void testRaceCondition() throws Exception {
        int positionLimit = 100;
        String ticker = "R";
        String buyer = "buyer";
        String seller = "seller";
        MatchingEngine engine = newEngine(positionLimit, ticker, buyer, seller);

        int iterations = 50;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        Thread tBid = new Thread(() -> {
            try {
                start.await();
                for (int i = 0; i < iterations; i++) {
                    engine.bidLimitOrder(buyer, new Order(buyer, ticker, 100, 1, Side.BID, Status.ACTIVE));
                }
            } catch (InterruptedException ignored) {
            } finally {
                done.countDown();
            }
        });

        Thread tAsk = new Thread(() -> {
            try {
                start.await();
                for (int i = 0; i < iterations; i++) {
                    engine.askLimitOrder(seller, new Order(seller, ticker, 100, 1, Side.ASK, Status.ACTIVE));
                }
            } catch (InterruptedException ignored) {
            } finally {
                done.countDown();
            }
        });

        tBid.start();
        tAsk.start();
        start.countDown();
        done.await();

        assertEquals(0, engine.getTickerBalance(buyer, ticker) + engine.getTickerBalance(seller, ticker));
        assertEquals(0, engine.getUserBalance(buyer) + engine.getUserBalance(seller));
        assertTrue(engine.getBidPriceLevels(ticker).isEmpty() || engine.getAskPriceLevels(ticker).isEmpty());
        assertEquals(100, engine.getPrice(ticker));
    }

    @Test
    void testUserListLimit() {
        int positionLimit = 10;
        String ticker = "L";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);
        long askOk = engine.askLimitOrder(user, new Order(user, ticker, 100, 6, Side.ASK, Status.ACTIVE));
        assertTrue(askOk > 0);
        long askReject = engine.askLimitOrder(user, new Order(user, ticker, 100, 5, Side.ASK, Status.ACTIVE));
        assertEquals(-1, askReject, "Second ask exceeds position limit when considering reserved ask size");
    }

    @Test
    void testCancelUserList() {
        int positionLimit = 10;
        String ticker = "CU";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        long id = engine.bidLimitOrder(user, new Order(user, ticker, 100, 7, Side.BID, Status.ACTIVE));
        assertTrue(id > 0);
        long reject = engine.bidLimitOrder(user, new Order(user, ticker, 100, 4, Side.BID, Status.ACTIVE));
        assertEquals(-1, reject);

        assertTrue(engine.removeOrder(user, id));
        long ok = engine.bidLimitOrder(user, new Order(user, ticker, 100, 4, Side.BID, Status.ACTIVE));
        assertTrue(ok > 0, "After cancel, reservation should free and allow new order");
    }

    @Test
    void testUserListMarket() {
        int positionLimit = 10;
        String ticker = "M";
        String seller = "s"; // will acquire inventory, then sell
        String counterparty = "c";
        MatchingEngine engine = newEngine(positionLimit, ticker, seller, counterparty);

        // Provide liquidity on the ask so seller can first buy inventory (within
        // positionLimit)
        engine.askLimitOrder(counterparty, new Order(counterparty, ticker, 100, 10, Side.ASK, Status.ACTIVE));
        int bought = engine.bidMarketOrder(seller, ticker, 10);
        assertEquals(10, bought);
        assertEquals(10, engine.getTickerBalance(seller, ticker));

        // Add bid liquidity to sell into
        engine.bidLimitOrder(counterparty, new Order(counterparty, ticker, 100, 20, Side.BID, Status.ACTIVE));

        // Attempt to sell over allowed amount (owned + positionLimit = 20) should be
        // rejected
        int filled = engine.askMarketOrder(seller, ticker, 25);
        assertEquals(0, filled);

        // Selling exactly owned amount should work
        filled = engine.askMarketOrder(seller, ticker, 10);
        assertEquals(10, filled);
        assertEquals(0, engine.getTickerBalance(seller, ticker));
    }

    @Test
    void testBidTrades() {
        int positionLimit = 1000;
        String ticker = "BT";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 7, Side.BID, Status.ACTIVE));

        @SuppressWarnings("unchecked")
        var trades = (List<PriceChange>) engine.getRecentTrades();
        boolean sawBidAt100With12 = false;
        for (PriceChange pc : trades) {
            if (pc.getTicker().equals(ticker) && pc.getPrice() == 100 && pc.getVolume() == 12
                    && pc.getSide() == Side.BID) {
                sawBidAt100With12 = true;
                break;
            }
        }
        assertTrue(sawBidAt100With12);
    }

    @Test
    void testRemoveAllOrdersRestoresBalances() {
        int positionLimit = 10;
        String ticker = "RA";
        String user = "u";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        long b1 = engine.bidLimitOrder(user, new Order(user, ticker, 100, 6, Side.BID, Status.ACTIVE));
        long b2 = engine.bidLimitOrder(user, new Order(user, ticker, 100, 4, Side.BID, Status.ACTIVE));
        assertTrue(b1 > 0 && b2 > 0);

        // Now any extra bid should fail due to reserved hitting limit
        long fail = engine.bidLimitOrder(user, new Order(user, ticker, 100, 1, Side.BID, Status.ACTIVE));
        assertEquals(-1, fail);

        engine.removeAll(user);

        // After removeAll, reservations should be cleared and new orders allowed
        long ok = engine.bidLimitOrder(user, new Order(user, ticker, 100, 1, Side.BID, Status.ACTIVE));
        assertTrue(ok > 0);

        // Ensure no actual balance/position changes (no trades happened)
        assertEquals(0, engine.getUserBalance(user));
        assertEquals(0, engine.getTickerBalance(user, ticker));
    }
}
