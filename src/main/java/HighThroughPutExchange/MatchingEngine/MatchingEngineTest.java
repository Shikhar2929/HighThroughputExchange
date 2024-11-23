package HighThroughPutExchange.MatchingEngine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class MatchingEngineTest {

    private static final double TOLERANCE = 1e-6;

    private boolean almostEqual(double a, double b) {
        return Math.abs(a - b) < TOLERANCE;
    }

    @Test
    public void testBidLimitOrder_AddsBidSuccessfully() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 1000.0);
        Order bidOrder = new Order("TraderA", 100.0, 10.0, Side.BID, Status.ACTIVE);

        long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
        assertTrue(orderId > 0, "Order ID should be valid and greater than 0");

        Map<Double, Deque<Order>> bids = engine.getBids();
        assertTrue(bids.containsKey(bidOrder.price), "Bid map should contain the order price level");
        assertEquals(bidOrder.volume, bids.get(bidOrder.price).peek().volume, "Bid volume should match");
        assertEquals(bidOrder.name, bids.get(bidOrder.price).peek().name, "Bid name should match");
    }

    @Test
    public void testAskLimitOrder_AddsAskSuccessfully() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderB", 10000.0);
        Order askOrder = new Order("TraderB", 105.0, 15.0, Side.ASK, Status.ACTIVE);

        long orderId = engine.askLimitOrder(askOrder.name, askOrder);
        assertTrue(orderId > 0, "Order ID should be valid and greater than 0");

        Map<Double, Deque<Order>> asks = engine.getAsks();
        assertTrue(asks.containsKey(askOrder.price), "Ask map should contain the order price level");
        assertEquals(askOrder.volume, asks.get(askOrder.price).peek().volume, "Ask volume should match");
        assertEquals(askOrder.name, asks.get(askOrder.price).peek().name, "Ask name should match");
    }

    @Test
    public void testGetHighestBid_AfterMultipleBids() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 10000.0);
        engine.initializeUser("TraderB", 10000.0);
        engine.bidLimitOrder("TraderA", new Order("TraderA", 100.0, 10.0, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder("TraderB", new Order("TraderB", 105.0, 5.0, Side.BID, Status.ACTIVE));

        assertTrue(almostEqual(engine.getHighestBid(), 105.0), "Highest bid should be 105.0");
    }

    @Test
    public void testGetLowestAsk_AfterMultipleAsks() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 10000.0);
        engine.initializeUser("TraderB", 10000.0);
        engine.askLimitOrder("TraderA", new Order("TraderA", 110.0, 10.0, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder("TraderB", new Order("TraderB", 105.0, 5.0, Side.ASK, Status.ACTIVE));
        System.out.println(engine.getLowestAsk());
        assertTrue(almostEqual(engine.getLowestAsk(), 105.0), "Lowest ask should be 105.0");
    }

    @Test
    public void testMatchingBidAndAskOrders() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 30000.0);
        engine.initializeUser("TraderB", 30000.0);

        engine.bidLimitOrder("TraderA", new Order("TraderA", 105.0, 10.0, Side.BID, Status.ACTIVE));
        engine.askLimitOrder("TraderB", new Order("TraderB", 105.0, 10.0, Side.ASK, Status.ACTIVE));

        assertTrue(almostEqual(engine.getHighestBid(), 0.0), "Highest bid should be 0 after matching");
        System.out.println(engine.getLowestAsk());
        assertEquals(engine.getLowestAsk(), Double.POSITIVE_INFINITY, "Lowest ask should be 0 after matching");
    }

    @Test
    public void testInsertBid() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 1000.0);
        Order bidOrder = new Order("TraderA", 100.0, 10.0, Side.BID, Status.ACTIVE);

        long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
        assertTrue(orderId > 0, "Order ID should be valid and greater than 0");

        Map<Double, Deque<Order>> bids = engine.getBids();
        assertTrue(bids.containsKey(100.0), "Bids should contain the inserted order price");
        assertEquals(10.0, bids.get(100.0).peek().volume, "Bid volume should be 10.0");
    }

    @Test
    public void testInsertAsk() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderB", 1000.0);
        Order askOrder = new Order("TraderB", 100.0, 5.0, Side.ASK, Status.ACTIVE);

        long orderId = engine.askLimitOrder(askOrder.name, askOrder);
        assertTrue(orderId > 0, "Order ID should be valid and greater than 0");

        Map<Double, Deque<Order>> asks = engine.getAsks();
        assertTrue(asks.containsKey(100.0), "Asks should contain the inserted order price");
        assertEquals(5.0, asks.get(100.0).peek().volume, "Ask volume should be 5.0");
    }

    @Test
    public void testFillOrderCompletely() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 10000.0);
        engine.initializeUser("TraderB", 10000.0);
        Order askOrder = new Order("TraderA", 100.0, 5.0, Side.ASK, Status.ACTIVE);
        Order bidOrder = new Order("TraderB", 100.0, 5.0, Side.BID, Status.ACTIVE);

        engine.askLimitOrder(askOrder.name, askOrder);
        engine.bidLimitOrder(bidOrder.name, bidOrder);

        assertTrue(engine.getAsks().isEmpty(), "Asks should be empty after full match");
        assertTrue(engine.getBids().isEmpty(), "Bids should be empty after full match");
    }

    @Test
    public void testPartialFill() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 1000.0);
        engine.initializeUser("TraderB", 1000.0);

        Order askOrder = new Order("TraderA", 100.0, 5.0, Side.ASK, Status.ACTIVE);
        Order bidOrder = new Order("TraderB", 100.0, 10.0, Side.BID, Status.ACTIVE);

        engine.askLimitOrder(askOrder.name, askOrder);
        engine.bidLimitOrder(bidOrder.name, bidOrder);

        Map<Double, Deque<Order>> bids = engine.getBids();
        assertTrue(engine.getAsks().isEmpty(), "Asks should be empty after partial match");
        assertTrue(bids.containsKey(100.0), "Bids should contain the remaining bid at 100.0");
        assertEquals(5.0, bids.get(100.0).peek().volume, "Remaining bid volume should be 5.0 after partial fill");
    }

    @Test
    public void testRaceCondition() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 1000.0);
        engine.initializeUser("TraderB", 1000.0);
        engine.initializeUser("TraderC", 1000.0);

        Order ask1 = new Order("TraderA", 100.0, 5.0, Side.ASK, Status.ACTIVE);
        Order ask2 = new Order("TraderB", 100.0, 7.0, Side.ASK, Status.ACTIVE);
        Order bid = new Order("TraderC", 100.0, 6.0, Side.BID, Status.ACTIVE);

        engine.askLimitOrder(ask1.name, ask1);
        engine.askLimitOrder(ask2.name, ask2);
        engine.bidLimitOrder(bid.name, bid);

        Map<Double, Deque<Order>> asks = engine.getAsks();
        assertTrue(asks.containsKey(100.0), "Asks should contain the remaining volume at 100.0");
        assertEquals(6.0, asks.get(100.0).peek().volume, "Remaining ask volume should be 6.0 after partial fill");
    }

    @Test
    public void testDifferentPricesNoMatch() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 10000.0);
        engine.initializeUser("TraderB", 10000.0);

        Order bid = new Order("TraderA", 95.0, 10.0, Side.BID, Status.ACTIVE);
        Order ask = new Order("TraderB", 105.0, 5.0, Side.ASK, Status.ACTIVE);

        engine.bidLimitOrder(bid.name, bid);
        engine.askLimitOrder(ask.name, ask);

        Map<Double, Deque<Order>> bids = engine.getBids();
        Map<Double, Deque<Order>> asks = engine.getAsks();
        assertTrue(bids.containsKey(95.0), "Bids should contain the unmatched bid at 95.0");
        assertTrue(asks.containsKey(105.0), "Asks should contain the unmatched ask at 105.0");
    }
    @Test
    public void testCancelOrderValidBid() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 10000.0);
        Order bid = new Order("TraderA", 100.0, 10.0, Side.BID, Status.ACTIVE);

        // Place and then cancel a bid order
        long orderId = engine.bidLimitOrder(bid.name, bid);
        boolean cancelStatus = engine.removeOrder("TraderA", orderId);

        Map<Double, Deque<Order>> bids = engine.getBids();
        assertTrue(cancelStatus, "Cancel should be successful for a valid bid order");
        //assertFalse(bids.containsKey(100.0), "Bids should not contain the canceled bid at 100.0");
    }

    @Test
    public void testCancelOrderValidAsk() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderB", 10000.0);
        Order ask = new Order("TraderB", 150.0, 20.0, Side.ASK, Status.ACTIVE);

        // Place and then cancel an ask order
        long orderId = engine.askLimitOrder(ask.name, ask);
        boolean cancelStatus = engine.removeOrder("TraderB", orderId);

        Map<Double, Deque<Order>> asks = engine.getAsks();
        assertTrue(cancelStatus, "Cancel should be successful for a valid ask order");
        //assertEquals(Status.CANCELLED, ask.status, "Order should be cancelled");
    }

    @Test
    public void testCancelOrderNonExistent() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("TraderA", 1000.0);
        // Attempt to cancel a non-existent order
        boolean cancelStatus = engine.removeOrder("TraderA", 9999); // assuming 9999 is an ID that doesn't exist
        assertFalse(cancelStatus, "Cancel should fail for a non-existent order ID");
    }
    @Test
    void testBidPriceLevelsAfterAddingLimitOrder() {
        // Add a bid order
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Trader1", 1000.0);
        Order bidOrder1 = new Order("Trader1", 100.0, 5.0, Side.BID, Status.ACTIVE);
        engine.bidLimitOrder(bidOrder1.name, bidOrder1);

        // Verify the bid price level
        List<PriceLevel> bidLevels = engine.getBidPriceLevels();
        assertEquals(1, bidLevels.size());
        assertEquals(100.0, bidLevels.get(0).price);
        assertEquals(5.0, bidLevels.get(0).volume);
    }
    @Test
    public void testAskPriceLevelsAfterAddingLimitOrder() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Trader2", 1000.0);
        Order askOrder1 = new Order("Trader2", 105.0, 3.0, Side.ASK, Status.ACTIVE);

        engine.askLimitOrder(askOrder1.name, askOrder1);

        List<PriceLevel> askLevels = engine.getAskPriceLevels();
        assertEquals(1, askLevels.size(), "There should be exactly one ask price level");
        assertEquals(105.0, askLevels.get(0).price, "Ask price level should match the inserted order's price");
        assertEquals(3.0, askLevels.get(0).volume, "Ask volume at the level should match the inserted order's volume");
    }

    @Test
    public void testPriceLevelsAfterPartialFill() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Trader1", 10000.0);
        engine.initializeUser("Trader2", 10000.0);
        Order askOrder1 = new Order("Trader2", 100.0, 5.0, Side.ASK, Status.ACTIVE);
        Order bidOrder1 = new Order("Trader1", 100.0, 3.0, Side.BID, Status.ACTIVE);

        engine.askLimitOrder(askOrder1.name, askOrder1);
        engine.bidLimitOrder(bidOrder1.name, bidOrder1);

        List<PriceLevel> askLevels = engine.getAskPriceLevels();
        assertEquals(1, askLevels.size(), "There should be one remaining ask price level after partial fill");
        assertEquals(100.0, askLevels.get(0).price, "Ask price level should remain unchanged after partial fill");
        assertEquals(2.0, askLevels.get(0).volume, "Remaining ask volume should be correct after partial fill");

        List<PriceLevel> bidLevels = engine.getBidPriceLevels();
        assertEquals(0, bidLevels.size(), "There should be no bid levels remaining after full fill of the bid order");
    }

    @Test
    public void testPriceLevelsAfterFullFill() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Trader1", 1000.0);
        engine.initializeUser("Trader2", 1000.0);
        Order askOrder1 = new Order("Trader2", 100.0, 5.0, Side.ASK, Status.ACTIVE);
        Order bidOrder1 = new Order("Trader1", 100.0, 5.0, Side.BID, Status.ACTIVE);

        engine.askLimitOrder(askOrder1.name, askOrder1);
        engine.bidLimitOrder(bidOrder1.name, bidOrder1);

        List<PriceLevel> askLevels = engine.getAskPriceLevels();
        assertEquals(0, askLevels.size(), "There should be no remaining ask price levels after full fill");

        List<PriceLevel> bidLevels = engine.getBidPriceLevels();
        assertEquals(0, bidLevels.size(), "There should be no remaining bid price levels after full fill");
    }

    @Test
    public void testPriceLevelsWithMultipleOrdersAtSamePrice() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Trader1", 1000.0);
        engine.initializeUser("Trader2", 1000.0);
        engine.initializeUser("Trader3", 1000.0);
        Order bidOrder1 = new Order("Trader1", 100.0, 3.0, Side.BID, Status.ACTIVE);
        Order bidOrder2 = new Order("Trader2", 100.0, 2.0, Side.BID, Status.ACTIVE);

        engine.bidLimitOrder(bidOrder1.name, bidOrder1);
        engine.bidLimitOrder(bidOrder2.name, bidOrder2);

        List<PriceLevel> bidLevels = engine.getBidPriceLevels();
        assertEquals(1, bidLevels.size(), "There should be exactly one bid price level");
        assertEquals(100.0, bidLevels.get(0).price, "Bid price level should match the inserted orders' price");
        assertEquals(5.0, bidLevels.get(0).volume, "Bid volume should be aggregated across orders at the same price");

        Order askOrder1 = new Order("Trader3", 100.0, 4.0, Side.ASK, Status.ACTIVE);
        engine.askLimitOrder(askOrder1.name, askOrder1);

        bidLevels = engine.getBidPriceLevels();
        assertEquals(1, bidLevels.size(), "One bid price level should remain after partial match");
        assertEquals(100.0, bidLevels.get(0).price, "Bid price level should remain unchanged");
        assertEquals(1.0, bidLevels.get(0).volume, "Remaining bid volume should be correct after partial fill");

        List<PriceLevel> askLevels = engine.getAskPriceLevels();
        assertEquals(0, askLevels.size(), "Ask price levels should be empty after full match");
    }

    @Test
    public void testRemovingOrderUpdatesPriceLevels() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Trader1", 1000.0);
        Order bidOrder1 = new Order("Trader1", 100.0, 3.0, Side.BID, Status.ACTIVE);

        long orderId = engine.bidLimitOrder(bidOrder1.name, bidOrder1);
        boolean removed = engine.removeOrder(bidOrder1.name, orderId);

        assertTrue(removed, "Order should be successfully removed");
        List<PriceLevel> bidLevels = engine.getBidPriceLevels();
        assertEquals(0, bidLevels.size(), "Bid price level should be removed after order cancellation");
    }
    @Test
    void testMarketBuyOrderFullyFilled() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Buyer1", 100000.0);
        engine.initializeUser("Seller1", 100000.0);
        engine.initializeUser("Seller2", 100000.0);


        // Setup initial asks
        engine.askLimitOrder("Seller1", new Order("Seller1", 100.0, 200.0, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder("Seller2", new Order("Seller2", 101.0, 400.0, Side.ASK, Status.ACTIVE));

        // Market Buy Order
        double filledVolume = engine.bidMarketOrder("Buyer1", 500.0);

        // Assert filled volume
        assertEquals(500.0, filledVolume);

        // Assert remaining asks
        assertEquals(101.0, engine.getLowestAsk());
        //assertEquals(0.0, engine.getOrder("Seller1", 1).volume);
        //assertTrue(engine.getOrder("Seller1", 1).status == Status.FILLED);
        assertEquals(100.0, engine.getOrder("Seller2", 2).volume);
    }

    @Test
    void testMarketSellOrderFullyFilled() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Buyer1", 100000.0);
        engine.initializeUser("Buyer2", 100000.0);
        engine.initializeUser("Seller1", 10000.0);
        // Setup initial bids
        engine.bidLimitOrder("Buyer1", new Order("Buyer1", 99.0, 150.0, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder("Buyer2", new Order("Buyer2", 98.0, 200.0, Side.BID, Status.ACTIVE));

        // Market Sell Order
        double filledVolume = engine.askMarketOrder("Seller1", 300.0);

        // Assert filled volume
        assertEquals(300.0, filledVolume);

        // Assert remaining bids
        assertEquals(98.0, engine.getHighestBid());
        assertEquals(0.0, engine.getOrder("Buyer1", 1).volume);
        assertEquals(50.0, engine.getOrder("Buyer2", 2).volume);
    }

    @Test
    void testMarketBuyOrderPartiallyFilled() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Seller1", 1000.0);
        engine.initializeUser("Buyer1", 100000.0);

        // Setup initial asks
        engine.askLimitOrder("Seller1", new Order("Seller1", 100.0, 200.0, Side.ASK, Status.ACTIVE));

        // Market Buy Order
        double filledVolume = engine.bidMarketOrder("Buyer1", 500.0);

        // Assert filled volume
        assertEquals(200.0, filledVolume);

        // Assert remaining order state
        assertEquals(Double.POSITIVE_INFINITY, engine.getLowestAsk());
        assertEquals(0.0, engine.getOrder("Seller1", 1).volume);
        assertEquals(Status.FILLED, engine.getOrder("Seller1", 1).status);
    }

    @Test
    void testMarketSellOrderPartiallyFilled() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Buyer1", 30000.0);
        engine.initializeUser("Seller1", 30000.0);

        // Setup initial bids
        engine.bidLimitOrder("Buyer1", new Order("Buyer1", 99.0, 300.0, Side.BID, Status.ACTIVE));

        // Market Sell Order
        double filledVolume = engine.askMarketOrder("Seller1", 500.0);

        // Assert filled volume
        assertEquals(300.0, filledVolume);

        // Assert remaining order state
        assertEquals(0, engine.getHighestBid());
        assertEquals(0.0, engine.getOrder("Buyer1", 1).volume);
        assertEquals(Status.FILLED, engine.getOrder("Buyer1", 1).status);
    }

    @Test
    void testMarketOrderCancelledDueToNoLiquidity() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Buyer1", 10000.0);
        engine.initializeUser("Seller1", 10000.0);

        // Market Buy Order with no asks
        double buyFilledVolume = engine.bidMarketOrder("Buyer1", 500.0);

        // Market Sell Order with no bids
        double sellFilledVolume = engine.askMarketOrder("Seller1", 300.0);

        // Assert filled volume
        assertEquals(0.0, buyFilledVolume);
        assertEquals(0.0, sellFilledVolume);
    }
    @Test
    void testUninitializedUser() {
        MatchingEngine engine = new MatchingEngine();
        long orderId = engine.bidLimitOrder("Buyer1", new Order("Buyer1", 99.0, 150.0, Side.BID, Status.ACTIVE));
        assertEquals(orderId, -1); // -1 represents invalid user
        orderId = engine.askLimitOrder("RandomName", new Order("RandomName", 5.0, 5.0, Side.ASK, Status.ACTIVE));
        assertEquals(orderId, -1);
        engine.initializeUser("ActualUser", 1000.0);
        engine.askLimitOrder("ActualUser", new Order("ActualUser", 10.0, 5.0, Side.ASK, Status.ACTIVE));
        engine.bidLimitOrder("ActualUser", new Order("ActualUser", 8.0, 5.0, Side.BID, Status.ACTIVE));
        double orderFilledVolume = engine.bidMarketOrder("Buyer1", 1000.0);
        assertEquals(orderFilledVolume, 0.0);
        orderFilledVolume = engine.bidMarketOrder("Rasdf;", 100);
        assertEquals(orderFilledVolume, 0.0);
        assertEquals(engine.getBidPriceLevels().size(), 1);
        assertEquals(engine.getAskPriceLevels().size(), 1);
    }
    @Test
    void testUserListLimit() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Trader1", 1000.0);
        engine.initializeUser("Trader2", 1000.0);
        assertEquals(engine.getUserBalance("Trader1"), 1000.0);
        Order bidOrder = new Order("Trader1", 100.0, 5.0, Side.BID, Status.ACTIVE);
        long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
        assertEquals(engine.getUserBalance("Trader1"), 500.0);
        Order askOrder = new Order("Trader2", 99.0, 5.0, Side.ASK, Status.ACTIVE);
        engine.askLimitOrder("Trader2", askOrder);
        assertEquals(engine.getUserBalance("Trader1"), 500.0);
        assertEquals(engine.getUserBalance("Trader2"), 1500.0);
        //
        Order askOrder2 = new Order("Trader1", 100.0, 5.0, Side.ASK, Status.ACTIVE);
        engine.askLimitOrder("Trader1", askOrder2);
        assertEquals(engine.getUserBalance("Trader1"), 500.0);
        assertEquals(engine.getUserBalance("Trader2"), 1500.0);
        Order bidOrder2 =  new Order("Trader2", 102.0, 5.0, Side.BID, Status.ACTIVE);
        engine.bidLimitOrder("Trader2", bidOrder2);
        assertEquals(engine.getUserBalance("Trader1"), 1000.0);
        assertEquals(engine.getUserBalance("Trader2"), 1000.0);
    }
    @Test
    void testCancelUserList() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Trader1", 1000.0);
        Order bidOrder = new Order("Trader1", 100.0, 5.0, Side.BID, Status.ACTIVE);
        long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
        assertEquals(engine.getUserBalance("Trader1"), 500.0);
        engine.removeOrder("Trader1", orderId);
        assertEquals(engine.getUserBalance("Trader1"), 1000.0);
    }

    @Test
    void testUserListMarket() {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeUser("Trader1", 1000.0);
        engine.initializeUser("Trader2", 500.0);
        Order bidOrder = new Order("Trader1", 100.0, 10.0, Side.BID, Status.ACTIVE);
        long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
        engine.askMarketOrder("Trader2", 5);
        assertEquals(engine.getUserBalance("Trader1"), 0.0);
        assertEquals(engine.getUserBalance("Trader2"), 1000.0);
        Order askOrder =  new Order("Trader1", 101.0, 10.0, Side.ASK, Status.ACTIVE);
        orderId = engine.askLimitOrder(askOrder.name, askOrder);
        double volume = engine.bidMarketOrder("Trader2", 10);
        assertEquals(9.0, volume);
        assertEquals(909, engine.getUserBalance("Trader1"));
        assertEquals(91, engine.getUserBalance("Trader2"));
    }

}
