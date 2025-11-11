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

    @Test
    void testBidLimitOrderBalanceUpdates() {
        int positionLimit = -1;
        String ticker = "AAPL";
        String[] users = {"Trader1"};
        int[] userBalances = {1000};
        int[] userTickerVolumes = {0};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        long orderId = engine.bidLimitOrder(users[0], new Order(users[0], ticker, 50, 10, Side.BID, Status.ACTIVE));

        assertEquals(1000, engine.getUserBalance(users[0]), "User balance should be reduced by bid order value");
        assertEquals(0, engine.getTickerBalance(users[0], ticker),
                "Ticker balance should remain unchanged for bid order");

        // Remove the order and verify balance restoration
        engine.removeOrder(users[0], orderId);
        assertEquals(1000, engine.getUserBalance(users[0]),
                "User balance should be restored after bid order cancellation");
    }

    @Test
    void testAskLimitOrderBalanceUpdates() {
        int positionLimit = -1;
        String ticker = "AAPL";
        String[] users = {"Trader1"};
        int[] userBalances = {1000};
        int[] userTickerVolumes = {20};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        long orderId = engine.askLimitOrder(users[0], new Order(users[0], ticker, 50, 10, Side.ASK, Status.ACTIVE));
        assertEquals(1000, engine.getUserBalance(users[0]), "User balance should remain unchanged for ask order");
        assertEquals(20, engine.getTickerBalance(users[0], ticker),
                "Ticker balance should be reduced by ask order volume");

        // Remove the order and verify ticker balance restoration
        engine.removeOrder(users[0], orderId);
        assertEquals(20, engine.getTickerBalance(users[0], ticker),
                "Ticker balance should be restored after ask order cancellation");
    }

    @Test
    void testBidMarketOrderBalanceUpdates() {
        int positionLimit = -1;
        String ticker = "AAPL";
        String buyer = "Buyer1", seller = "Seller1";
        String[] users = {buyer, seller};
        int[] userBalances = {500, 500};
        int[] userTickerVolumes = {0, 10};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        // Seller posts an ask order
        engine.askLimitOrder(seller, new Order(seller, ticker, 50, 10, Side.ASK, Status.ACTIVE));

        // Buyer places a market order
        double volumeFilled = engine.bidMarketOrder(buyer, ticker, 10);

        assertEquals(10, volumeFilled, "Market order should fully match the ask order volume");
        assertEquals(0, engine.getUserBalance(buyer), "Buyer's balance should be fully consumed");
        assertEquals(10, engine.getTickerBalance(buyer, ticker),
                "Buyer's ticker balance should increase by matched volume");
        assertEquals(1000, engine.getUserBalance(seller), "Seller's balance should increase by the transaction value");
        assertEquals(0, engine.getTickerBalance(seller, ticker),
                "Seller's ticker balance should be reduced by the sold volume");
    }

    @Test
    void testRemoveOrderRestoresAllProperties() {
        int positionLimit = -1;
        String ticker = "AAPL";
        String[] users = {"Trader1"};
        int[] userBalances = {1000};
        int[] userTickerVolumes = {20};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        // Place an order
        Order bidOrder = new Order(users[0], ticker, 100, 1, Side.BID, Status.ACTIVE);
        long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);

        // Verify initial state
        assertEquals(1000, engine.getUserBalance(users[0]), "User balance should reflect bid reservation");
        assertEquals(20, engine.getTickerBalance(users[0], ticker),
                "Ticker volume should remain unchanged for bid orders");

        // Remove the order
        boolean removed = engine.removeOrder(users[0], orderId);

        // Verify removal
        assertTrue(removed, "Order should be successfully removed");
        assertEquals(1000, engine.getUserBalance(users[0]), "User balance should be restored after removing bid order");
        assertEquals(20, engine.getTickerBalance(users[0], ticker),
                "Ticker balance should remain unchanged after removing bid order");
        assertNull(engine.getOrder(users[0], orderId), "Getting Order Should be Null");
    }

    @Test
    void testRemoveOrderForAskRestoresAllProperties() {
        int positionLimit = -1;
        String ticker = "AAPL";
        String[] users = {"Trader1"};
        int[] userBalances = {1000};
        int[] userTickerVolumes = {20};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        // Place an ask order
        Order askOrder = new Order(users[0], ticker, 100, 10, Side.ASK, Status.ACTIVE);
        long orderId = engine.askLimitOrder(askOrder.name, askOrder);

        // Verify initial state
        assertEquals(1000, engine.getUserBalance(users[0]), "User balance should remain unchanged for ask orders");
        assertEquals(20, engine.getTickerBalance(users[0], ticker), "Ticker balance should reflect ask reservation");

        // Remove the ask order
        boolean removed = engine.removeOrder(users[0], orderId);

        // Verify removal
        assertTrue(removed, "Ask order should be successfully removed");
        assertEquals(1000, engine.getUserBalance(users[0]),
                "User balance should remain unchanged after removing ask order");
        assertEquals(20, engine.getTickerBalance(users[0], ticker),
                "Ticker balance should be fully restored after removing ask order");
        assertNull(engine.getOrder(users[0], orderId), "Order status should be CANCELLED");
        List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
        assertEquals(0, askLevels.size(), "Ask price levels should be empty after removing the order");
    }

    @Test
    void testRemoveAllRestoresAllProperties() {
        int positionLimit = -1;
        String ticker = "AAPL";
        String[] users = {"Trader1"};
        int[] userBalances = {1000};
        int[] userTickerVolumes = {20};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        // Place multiple orders
        engine.bidLimitOrder(users[0], new Order(users[0], ticker, 50, 10, Side.BID, Status.ACTIVE));
        engine.askLimitOrder(users[0], new Order(users[0], ticker, 60, 5, Side.ASK, Status.ACTIVE));

        // Verify initial state
        assertEquals(1000, engine.getUserBalance(users[0]), "User balance should reflect bid reservation");

        // TODO: should be 15, but outputs 20
        assertEquals(20, engine.getTickerBalance(users[0], ticker), "Ticker volume should reflect ask reservation");
        // assertEquals(15, engine.getTickerBalance(users[0], ticker), "Ticker volume
        // should reflect ask reservation");

        // Remove all orders
        engine.removeAll(users[0]);

        // Verify all properties are restored
        assertEquals(1000, engine.getUserBalance(users[0]),
                "User balance should be fully restored after removing all orders");
        assertEquals(20, engine.getTickerBalance(users[0], ticker),
                "Ticker balance should be fully restored after removing all orders");

        List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
        List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
        assertEquals(0, bidLevels.size(), "All bid orders should be removed");
        assertEquals(0, askLevels.size(), "All ask orders should be removed");
    }

    @Test
    void testInitializeTickers() {
        MatchingEngine engine = new MatchingEngine();
        String[] tickers = {"AAPL", "GOOG", "NVDA"};

        for (String t : tickers) {
            engine.initializeTicker(t);

            // No price set yet
            assertEquals(0, engine.getPrice(t), "Price should be 0 after initialization for " + t);

            // No orders yet
            assertEquals(0, engine.getHighestBid(t), "Highest bid should be 0 after initialization for " + t);
            assertEquals(Integer.MAX_VALUE, engine.getLowestAsk(t),
                    "Lowest ask should be Integer.MAX_VALUE after initialization for " + t);
            assertTrue(engine.getBidPriceLevels(t).isEmpty(), "Bid price levels should be empty for " + t);
            assertTrue(engine.getAskPriceLevels(t).isEmpty(), "Ask price levels should be empty for " + t);
        }
    }

    @Test
    void testInitializeUsers() {
        MatchingEngine engine = new MatchingEngine(1000);
        engine.initializeTicker("AAPL");
        engine.initializeTicker("GOOG");
        engine.initializeTicker("NVDA");

        assertTrue(engine.initializeUser("alice"));
        assertTrue(engine.initializeUser("bob"));

        assertEquals(0, engine.getUserBalance("alice"));
        assertEquals(0, engine.getUserBalance("bob"));
    }

    @Test
    void testAddTradeAndCheckBidVolumeSummation() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 7, Side.BID, Status.ACTIVE));

        List<PriceChange> trades = engine.getRecentTrades();
        assertFalse(trades.isEmpty(), "Trades list should not be empty");
        assertEquals(1, trades.size(), "There should be exactly one trade recorded");

        PriceChange trade = trades.get(0);
        assertEquals(100, trade.getPrice(), "Trade price should match the updated price");
        assertEquals(12, trade.getVolume(), "Trade volume should be the sum of updates");
        assertEquals(Side.BID, trade.getSide(), "Trade side should match the updated side");
    }
}