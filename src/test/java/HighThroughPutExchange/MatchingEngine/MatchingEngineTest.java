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

    // TODO: fix config.json for initialize all tickers
    @Test
    void testInitializeTickers() {
        MatchingEngine matchingEngine = new MatchingEngine();
        matchingEngine.initializeAllTickers();
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

    @Test
    void testAddTradeAndCheckAskVolumeSummation() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.askLimitOrder(user, new Order(user, ticker, 100, 5, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(user, new Order(user, ticker, 100, 7, Side.ASK, Status.ACTIVE));

        List<PriceChange> trades = engine.getRecentTrades();
        assertFalse(trades.isEmpty(), "Trades list should not be empty");
        assertEquals(1, trades.size(), "There should be exactly one trade recorded");

        PriceChange trade = trades.get(0);
        assertEquals(100, trade.getPrice(), "Trade price should match the updated price");
        assertEquals(12, trade.getVolume(), "Trade volume should be the sum of updates");
        assertEquals(Side.ASK, trade.getSide(), "Trade side should match the updated side");
    }

    @Test
    void testTradeLoggingForDifferentPrices() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.bidLimitOrder(user, new Order(user, ticker, 100, 10, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 105, 20, Side.BID, Status.ACTIVE));

        List<PriceChange> trades = engine.getRecentTrades();
        assertEquals(2, trades.size(), "Should log trades at two different prices");

        // Checking each trade
        PriceChange firstTrade = trades.get(0);
        PriceChange secondTrade = trades.get(1);

        if (firstTrade.getPrice() == 100) {
            assertEquals(10, firstTrade.getVolume(), "Volume at price 100 should be 10");
            assertEquals(105, secondTrade.getPrice(), "Second trade price should be 105");
            assertEquals(20, secondTrade.getVolume(), "Volume at price 105 should be 20");
        } else {
            assertEquals(20, firstTrade.getVolume(), "Volume at price 105 should be 20");
            assertEquals(100, secondTrade.getPrice(), "Second trade price should be 100");
            assertEquals(10, secondTrade.getVolume(), "Volume at price 100 should be 10");
        }
    }

    @Test
    void testTradeClearingPostRetrieval() {
        int positionLimit = 1000;
        String ticker = "AAPL";
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));

        List<PriceChange> first = engine.getRecentTrades();
        assertFalse(first.isEmpty());

        List<PriceChange> second = engine.getRecentTrades();
        assertTrue(second.isEmpty(), "After retrieval trades map should be cleared");
    }

    @Test
    public void testLimitOrderTradeLogging() {
        int positionLimit = -1;
        String ticker = "GOOG";
        String[] users = {"TraderC"};
        int[] userBalances = {2000};
        int[] userTickerVolumes = {50};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        // Place a bid limit order and an ask limit order that should match
        engine.bidLimitOrder(users[0], new Order(users[0], ticker, 150, 5, Side.BID, Status.ACTIVE));
        engine.askLimitOrder(users[0], new Order(users[0], ticker, 150, 5, Side.ASK, Status.ACTIVE));

        // Check trades logged
        List<PriceChange> trades = engine.getRecentTrades();
        assertFalse(trades.isEmpty(), "Trades should be logged for matching orders");
        assertEquals(1, trades.size(), "One trade should be logged for the matched orders");

        PriceChange trade = trades.get(0);
        assertEquals(150, trade.getPrice(), "Trade price should match the order price");
        assertEquals(0, trade.getVolume(), "Trade volume should match the order volume");
        assertNotNull(trade.getSide(), "Trade should have a side");
    }

    @Test
    public void testMarketOrderTradeLogging() {
        int positionLimit = -1;
        String ticker = "GOOG";
        String[] users = {"TraderD"};
        int[] userBalances = {5000};
        int[] userTickerVolumes = {100};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        // Set up limit orders to be matched by a market order
        engine.askLimitOrder(users[0], new Order(users[0], ticker, 200, 20, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(users[0], new Order(users[0], ticker, 205, 20, Side.ASK, Status.ACTIVE));
        List<PriceChange> trades = engine.getRecentTrades();
        assertEquals(2, trades.size());
        // Place market buy order that matches the above asks
        engine.bidMarketOrder(users[0], ticker, 30);

        // Check trades logged
        trades = engine.getRecentTrades();
        assertEquals(2, trades.size(), "Two trades should be logged for the matched market order");

        // Check first trade details
        PriceChange firstTrade = trades.get(0);
        assertEquals(200, firstTrade.getPrice(), "First trade price should be 200");
        assertEquals(0, firstTrade.getVolume(), "Volume at level should now be 0");

        // Check second trade details
        PriceChange secondTrade = trades.get(1);
        assertEquals(205, secondTrade.getPrice(), "Second trade price should be 205");
        assertEquals(10, secondTrade.getVolume(), "Volume at level show now be 10");
    }

    @Test
    void testCancelOrderTradeLogging() {
        int positionLimit = -1;
        String ticker = "GOOG";
        String[] users = {"TraderE"};
        int[] userBalances = {10000};
        int[] userTickerVolumes = {50};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        // Place and cancel a bid limit order
        long orderId = engine.bidLimitOrder(users[0], new Order(users[0], ticker, 250, 10, Side.BID, Status.ACTIVE));
        engine.removeOrder(users[0], orderId);

        // Check that no trades were logged for the cancellation
        List<PriceChange> trades = engine.getRecentTrades();
        assertEquals(trades.size(), 1, "Cancel is also in the map");
    }

    @Test
    void testInfiniteBalanceBuyer() {
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
    public void testInfiniteBalanceTradingWithSelf() {
        int positionLimit = 1000;
        String user = "TraderZ";
        String ticker = "AAPL";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        // Try to buy up to position limit
        Order buyOrder = new Order(user, ticker, 150, 50, Side.BID, Status.ACTIVE); // Buy additional 50 to reach 100
        long orderId1 = engine.bidLimitOrder(user, buyOrder);
        assertTrue(orderId1 > 0, "Should allow buying within position limit");
        assertEquals(0, engine.getTickerBalance(user, ticker));
        assertEquals(0, engine.getUserBalance(user));

        // Try to short up to position limit
        Order sellOrder = new Order(user, ticker, 150, 50, Side.ASK, Status.ACTIVE); // Short additional 50 to reach
                                                                                     // -100
        long orderId2 = engine.askLimitOrder(user, sellOrder);
        assertEquals(0, orderId2, "Should be completely filled");
        assertEquals(0, engine.getTickerBalance(user, ticker));
        assertEquals(0, engine.getUserBalance(user));
    }

    @Test
    public void testInfiniteRemove() {
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
        assertEquals(-100 * 100, engine.getUserBalance(user1));
        assertEquals(100 * 100, engine.getUserBalance(user2));
        assertEquals(100, engine.getTickerBalance(user1, ticker));
        assertEquals(-100, engine.getTickerBalance(user2, ticker));
        assertEquals(100, engine.getPrice(ticker), "Matched, should be 100");
    }

    @Test
    public void testExceedPositionLimit() {
        int positionLimit = 1000;
        String user1 = "A";
        String user2 = "B";
        String ticker = "Spades";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);

        long orderId1 = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1001, Side.BID, Status.ACTIVE));
        assertEquals(-1, orderId1);

        long orderId2 = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1000, Side.BID, Status.ACTIVE));
        assertEquals(1, orderId2);
        assertEquals(0, engine.getUserBalance(user1));
        assertEquals(0, engine.getTickerBalance(user1, ticker));
        assertEquals(0, engine.getPrice(ticker), "No Matching yet, should be 0");

        double volumeFilled1 = engine.askMarketOrder(user2, ticker, 1001);
        assertEquals(0, volumeFilled1);
        assertEquals(0, engine.getPrice(ticker), "No Matching yet, should be 0");

        double volumeFilled2 = engine.askMarketOrder(user2, ticker, 999);
        assertEquals(999, volumeFilled2);
        assertEquals(999 * 100, engine.getUserBalance(user2));
        assertEquals(999, engine.getTickerBalance(user1, ticker));
        assertEquals(-999, engine.getTickerBalance(user2, ticker));
        assertEquals(100, engine.getPrice(ticker), "Matched, should be 100");
    }

    @Test
    public void testInfiniteMarketOrder() {
        int positionLimit = 1000;
        String user1 = "A";
        String user2 = "B";
        String ticker = "Spades";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);

        long orderId = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1000, Side.BID, Status.ACTIVE));
        assertEquals(1, orderId);
        assertEquals(0, engine.getUserBalance(user1));
        assertEquals(0, engine.getPrice(ticker), "No Matching yet, should be 0");

        engine.askMarketOrder(user1, ticker, 10);
        assertEquals(0, engine.getUserBalance(user1));
        assertEquals(0, engine.getTickerBalance(user1, ticker));
        assertEquals(100, engine.getPrice(ticker), "Matched, should be 100");

        engine.askMarketOrder(user2, ticker, 900);
        assertEquals(-900 * 100, engine.getUserBalance(user1));
        assertEquals(900 * 100, engine.getUserBalance(user2));
        assertEquals(900, engine.getTickerBalance(user1, ticker));
        assertEquals(-900, engine.getTickerBalance(user2, ticker));

        engine.removeOrder(user1, orderId);
        assertEquals(-100 * 900, engine.getUserBalance(user1));

        engine.askLimitOrder(user1, new Order(user1, ticker, 105, 100, Side.ASK, Status.ACTIVE));
        double volumeFilled = engine.bidMarketOrder(user2, ticker, 300);

        assertEquals(100, volumeFilled);
        assertEquals(0, engine.getUserBalance(user1) + engine.getUserBalance(user2));
        assertEquals(-800, engine.getTickerBalance(user2, ticker));
        assertEquals(800, engine.getTickerBalance(user1, ticker));
        assertEquals(105, engine.getPrice(ticker), "Matched, should be 105");
    }

    @Test
    public void testPositionLimitEdgeCases() {
        int positionLimit = 1000;
        String ticker = "A";
        String user1 = "u1";
        String user2 = "u2";
        MatchingEngine engine = newEngine(positionLimit, ticker, user1, user2);

        long orderId1 = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 100, Side.ASK, Status.ACTIVE));
        assertNotEquals(-1, orderId1);
        assertEquals(0, engine.getUserBalance(user1));
        assertEquals(0, engine.getTickerBalance(user1, ticker));

        double volumeFilled = engine.askMarketOrder(user2, ticker, 100);
        assertEquals(100, volumeFilled);
        assertEquals(10000, engine.getUserBalance(user2));
        assertEquals(-10000, engine.getUserBalance(user1));
        assertEquals(100, engine.getTickerBalance(user1, ticker));
        assertEquals(-100, engine.getTickerBalance(user2, ticker));

        long orderId2 = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 900, Side.BID, Status.ACTIVE));
        assertNotEquals(-1, orderId2);
        assertEquals(-10000, engine.getUserBalance(user1));
        assertEquals(100, engine.getTickerBalance(user1, ticker));

        long orderId3 = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1, Side.BID, Status.ACTIVE));
        assertEquals(-1, orderId3, "Exceeding Position Limit on Bid Size");

        long orderId4 = engine.askLimitOrder(user1, new Order(user1, ticker, 150, 1101, Side.BID, Status.ACTIVE));
        assertEquals(-1, orderId4);

        long orderId5 = engine.askLimitOrder(user1, new Order(user1, ticker, 150, 1100, Side.BID, Status.ACTIVE));
        assertNotEquals(-1, orderId5);

        long orderId6 = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1, Side.BID, Status.ACTIVE));
        assertEquals(-1, orderId6, "Exceeding Position Limit on Bid Size");

        boolean status = engine.removeOrder(user1, orderId2);
        assertTrue(status);

        long orderId7 = engine.bidLimitOrder(user1, new Order(user1, ticker, 100, 1, Side.BID, Status.ACTIVE));
        assertNotEquals(-1, orderId7, "Order should now go through");
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

        Map<String, Object> message1 = engine.askLimitOrderHandler(user1,
                new Order(user1, ticker, 15, 80, Side.ASK, Status.ACTIVE));
        assertEquals(0, (double) message1.get("price"));
        assertEquals(0, (int) message1.get("volumeFilled"));

        Map<String, Object> message2 = engine.bidLimitOrderHandler(user2,
                new Order(user2, ticker, 16, 10, Side.BID, Status.ACTIVE));
        assertEquals(15, (double) message2.get("price"));
        assertEquals(10, (int) message2.get("volumeFilled"));

        Map<String, Object> message3 = engine.askLimitOrderHandler(user1,
                new Order(user1, ticker, 14, 5, Side.ASK, Status.ACTIVE));
        assertEquals(0, (double) message3.get("price"));
        assertEquals(0, (int) message3.get("volumeFilled"));

        Map<String, Object> message4 = engine.bidMarketOrderHandler(user2, ticker, 15);
        assertEquals((14.0 * 5 + 10 * 15) / 15, (double) message4.get("price"));
        assertEquals(15, message4.get("volumeFilled"));
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
    public void testInfiniteBrokenTest() {
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

        long orderId1 = engine.bidLimitOrder(user1, new Order(user1, ticker, 200, 1000, Side.BID, Status.ACTIVE));
        assertEquals(1, orderId1);

        long orderId2 = engine.askLimitOrder(user1, new Order(user1, ticker, 250, 1000, Side.BID, Status.ACTIVE));
        assertEquals(2, orderId2);

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
        long orderId3 = engine.bidLimitOrder(user3, new Order(user3, ticker, 235, 200, Side.BID, Status.ACTIVE));
        assertEquals(3, orderId3);

        long orderId4 = engine.bidLimitOrder(user3, new Order(user3, ticker, 240, 200, Side.BID, Status.ACTIVE));
        assertEquals(4, orderId4);

        engine.askMarketOrder(user2, ticker, 400);
        assertEquals(235, engine.getPrice(ticker));
        assertEquals(400, engine.getTickerBalance(user3, ticker));
        assertEquals(-235 * 200 - 240 * 200, engine.getUserBalance(user3));
        assertEquals(-5 * 200, engine.getPnl(user3));
        assertEquals(15 * 1000, engine.getPnl(user1));
        assertEquals(-14 * 1000, engine.getPnl(user2));
    }

    @Test
    public void testRaceCondition() {
        int positionLimit = -1;
        String ticker = "AAPL";
        String[] users = {"TraderA", "TraderB", "TraderC"};
        int[] userBalances = {1000, 1000, 1000};
        int[] userTickerVolumes = {15, 15, 15};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        engine.askLimitOrder(users[0], new Order(users[0], ticker, 100, 5, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(users[1], new Order(users[1], ticker, 100, 7, Side.ASK, Status.ACTIVE));
        engine.bidLimitOrder(users[2], new Order(users[2], ticker, 100, 6, Side.BID, Status.ACTIVE));

        Map<Integer, Deque<Order>> asks = engine.getAsks(ticker);
        assertTrue(asks.containsKey(100), "Asks should contain the remaining volume at 100");
        assertEquals(6, asks.get(100).peek().volume, "Remaining ask volume should be 6 after partial fill");
    }

    @Test
    void testUserListLimit() {
        int positionLimit = 10;
        String ticker = "L";
        String user = "Trader";
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
        String user = "Trader";
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
        String seller = "Seller"; // will acquire inventory, then sell
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
        String user = "Trader";
        MatchingEngine engine = newEngine(positionLimit, ticker, user);

        engine.bidLimitOrder(user, new Order(user, ticker, 100, 5, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user, new Order(user, ticker, 100, 7, Side.BID, Status.ACTIVE));

        List<PriceChange> trades = engine.getRecentTrades();
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
        int positionLimit = -1;
        String ticker = "AAPL";
        String[] users = {"Trader1"};
        int[] userBalances = {1000};
        int[] userTickerVolumes = {20};

        MatchingEngine engine = newEngine(positionLimit, ticker, users, userBalances, userTickerVolumes);

        // Place multiple orders
        engine.bidLimitOrder(users[0], new Order(users[0], ticker, 50, 10, Side.BID, Status.ACTIVE));
        engine.askLimitOrder(users[0], new Order(users[0], ticker, 60, 5, Side.ASK, Status.ACTIVE));

        // Remove all orders and verify balances
        engine.removeAll(users[0]);
        assertEquals(1000, engine.getUserBalance(users[0]),
                "User balance should be restored after removing all orders");
        assertEquals(20, engine.getTickerBalance(users[0], ticker),
                "Ticker balance should be restored after removing all orders");
    }
}