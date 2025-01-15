    package HighThroughPutExchange.MatchingEngine;
    import org.junit.jupiter.api.Test;
    import static org.junit.jupiter.api.Assertions.*;
    
    import java.util.*;
    
    public class MatchingEngineTest {
    
        private static final double TOLERANCE = 1e-6;
    
        private boolean almostEqual(double a, double b) {
            return Math.abs(a - b) < TOLERANCE;
        }
    
        @Test
        public void testBidLimitOrder_AddsBidSuccessfully() {
            MatchingEngine engine = new MatchingEngine();
            engine.initializeUserBalance("TraderA", 1000.0);
            String ticker = "AAPL";
            engine.initializeTicker(ticker);
            engine.initializeUserTickerVolume("TraderA", ticker, 10.0);
            Order bidOrder = new Order("TraderA", ticker,100.0, 10.0, Side.BID, Status.ACTIVE);
    
            long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
            assertTrue(orderId > 0, "Order ID should be valid and greater than 0");
    
            Map<Double, Deque<Order>> bids = engine.getBids(ticker);
            assertTrue(bids.containsKey(bidOrder.price), "Bid map should contain the order price level");
            assertEquals(bidOrder.volume, bids.get(bidOrder.price).peek().volume, "Bid volume should match");
            assertEquals(bidOrder.name, bids.get(bidOrder.price).peek().name, "Bid name should match");
        }
    
        @Test
        public void testAskLimitOrder_AddsAskSuccessfully() {
            MatchingEngine engine = new MatchingEngine();
    
            engine.initializeUserBalance("TraderB", 10000.0);
            String ticker = "AAPL";
            engine.initializeTicker(ticker);
            engine.initializeUserTickerVolume("TraderB", ticker, 105.0);
            Order askOrder = new Order("TraderB", ticker, 105.0, 15.0, Side.ASK, Status.ACTIVE);
    
            long orderId = engine.askLimitOrder(askOrder.name, askOrder);
            assertTrue(orderId > 0, "Order ID should be valid and greater than 0");
    
            Map<Double, Deque<Order>> asks = engine.getAsks(ticker);
            assertTrue(asks.containsKey(askOrder.price), "Ask map should contain the order price level");
            assertEquals(askOrder.volume, asks.get(askOrder.price).peek().volume, "Ask volume should match");
            assertEquals(askOrder.name, asks.get(askOrder.price).peek().name, "Ask name should match");
        }
    
        @Test
        public void testGetHighestBid_AfterMultipleBids() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker(ticker);
            engine.initializeUserBalance("TraderA", 10000.0);
            engine.initializeUserBalance("TraderB", 10000.0);
            engine.bidLimitOrder("TraderA", new Order("TraderA", ticker, 100.0, 10.0, Side.BID, Status.ACTIVE));
            engine.bidLimitOrder("TraderB", new Order("TraderB", ticker, 105.0, 5.0, Side.BID, Status.ACTIVE));
    
            assertTrue(almostEqual(engine.getHighestBid(ticker), 105.0), "Highest bid should be 105.0");
        }
    
        @Test
        public void testGetLowestAsk_AfterMultipleAsks() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("TraderA", 10000.0);
            engine.initializeUserBalance("TraderB", 10000.0);
            engine.initializeUserTickerVolume("TraderA", ticker, 10.0);
            engine.initializeUserTickerVolume("TraderB", ticker, 10.0);
    
            engine.askLimitOrder("TraderA", new Order("TraderA", ticker, 110.0, 10.0, Side.ASK, Status.ACTIVE));
            engine.askLimitOrder("TraderB", new Order("TraderB", ticker, 105.0, 5.0, Side.ASK, Status.ACTIVE));
            System.out.println(engine.getLowestAsk(ticker));
            assertTrue(almostEqual(engine.getLowestAsk(ticker), 105.0), "Lowest ask should be 105.0");
        }
    
        @Test
        public void testMatchingBidAndAskOrders() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("TraderA", 30000.0);
            engine.initializeUserBalance("TraderB", 30000.0);
            engine.initializeUserTickerVolume("TraderA", ticker, 0.0);
            engine.initializeUserTickerVolume("TraderB", ticker, 10.0);
            engine.bidLimitOrder("TraderA", new Order("TraderA", ticker, 105.0, 10.0, Side.BID, Status.ACTIVE));
            engine.askLimitOrder("TraderB", new Order("TraderB", ticker,105.0, 10.0, Side.ASK, Status.ACTIVE));
    
            assertTrue(almostEqual(engine.getHighestBid(ticker), 0.0), "Highest bid should be 0 after matching");
            System.out.println(engine.getLowestAsk(ticker));
            assertEquals(engine.getLowestAsk(ticker), Double.POSITIVE_INFINITY, "Lowest ask should be 0 after matching");
        }
    
        @Test
        public void testInsertBid() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("TraderA", 1000.0);
            Order bidOrder = new Order("TraderA", ticker, 100.0, 10.0, Side.BID, Status.ACTIVE);
    
            long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
            assertTrue(orderId > 0, "Order ID should be valid and greater than 0");
    
            Map<Double, Deque<Order>> bids = engine.getBids(ticker);
            assertTrue(bids.containsKey(100.0), "Bids should contain the inserted order price");
            assertEquals(10.0, bids.get(100.0).peek().volume, "Bid volume should be 10.0");
        }
    
        @Test
        public void testInsertAsk() {
            String ticker = "AAPL";
            MatchingEngine engine = new MatchingEngine();
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("TraderB", 1000.0);
            engine.initializeUserTickerVolume("TraderB", ticker, 10.0);
            Order askOrder = new Order("TraderB", ticker,100.0, 5.0, Side.ASK, Status.ACTIVE);
    
            long orderId = engine.askLimitOrder(askOrder.name, askOrder);
            assertTrue(orderId > 0, "Order ID should be valid and greater than 0");
    
            Map<Double, Deque<Order>> asks = engine.getAsks(ticker);
            assertTrue(asks.containsKey(100.0), "Asks should contain the inserted order price");
            assertEquals(5.0, asks.get(100.0).peek().volume, "Ask volume should be 5.0");
        }
    
        @Test
        public void testFillOrderCompletely() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("TraderA", 10000.0);
            engine.initializeUserBalance("TraderB", 10000.0);
            engine.initializeUserTickerVolume("TraderA", "AAPL", 5.0);
            engine.initializeUserTickerVolume("TraderB", ticker, 0.0);
            Order askOrder = new Order("TraderA", ticker, 100.0, 5.0, Side.ASK, Status.ACTIVE);
            Order bidOrder = new Order("TraderB", ticker, 100.0, 5.0, Side.BID, Status.ACTIVE);
    
            engine.askLimitOrder(askOrder.name, askOrder);
            engine.bidLimitOrder(bidOrder.name, bidOrder);
    
            assertTrue(engine.getAsks(ticker).isEmpty(), "Asks should be empty after full match");
            assertTrue(engine.getBids(ticker).isEmpty(), "Bids should be empty after full match");
        }
    
        @Test
        public void testPartialFill() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("TraderA", 1000.0);
            engine.initializeUserBalance("TraderB", 1000.0);
            engine.initializeUserTickerVolume("TraderA", ticker, 5.0);
            engine.initializeUserTickerVolume("TraderB", ticker, 0.0);
            Order askOrder = new Order("TraderA", ticker, 100.0, 5.0, Side.ASK, Status.ACTIVE);
            Order bidOrder = new Order("TraderB", ticker, 100.0, 10.0, Side.BID, Status.ACTIVE);
    
            engine.askLimitOrder(askOrder.name, askOrder);
            engine.bidLimitOrder(bidOrder.name, bidOrder);
    
            Map<Double, Deque<Order>> bids = engine.getBids(ticker);
            assertTrue(engine.getAsks(ticker).isEmpty(), "Asks should be empty after partial match");
            assertTrue(bids.containsKey(100.0), "Bids should contain the remaining bid at 100.0");
            assertEquals(5.0, bids.get(100.0).peek().volume, "Remaining bid volume should be 5.0 after partial fill");
        }
    
        @Test
        public void testRaceCondition() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("TraderA", 1000.0);
            engine.initializeUserBalance("TraderB", 1000.0);
            engine.initializeUserBalance("TraderC", 1000.0);
            engine.initializeUserTickerVolume("TraderA", ticker, 15.0);
            engine.initializeUserTickerVolume("TraderB", ticker, 15.0);
            engine.initializeUserTickerVolume("TraderC", ticker, 15.0);
    
            Order ask1 = new Order("TraderA", ticker, 100.0, 5.0, Side.ASK, Status.ACTIVE);
            Order ask2 = new Order("TraderB", ticker, 100.0, 7.0, Side.ASK, Status.ACTIVE);
            Order bid = new Order("TraderC", ticker, 100.0, 6.0, Side.BID, Status.ACTIVE);
    
            engine.askLimitOrder(ask1.name, ask1);
            engine.askLimitOrder(ask2.name, ask2);
            engine.bidLimitOrder(bid.name, bid);
    
            Map<Double, Deque<Order>> asks = engine.getAsks(ticker);
            assertTrue(asks.containsKey(100.0), "Asks should contain the remaining volume at 100.0");
            assertEquals(6.0, asks.get(100.0).peek().volume, "Remaining ask volume should be 6.0 after partial fill");
        }
    
        @Test
        public void testDifferentPricesNoMatch() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("TraderA", 10000.0);
            engine.initializeUserBalance("TraderB", 10000.0);
            engine.initializeUserTickerVolume("TraderA", ticker, 10.0);
            engine.initializeUserTickerVolume("TraderB", ticker, 10.0);
            Order bid = new Order("TraderA", ticker, 95.0, 10.0, Side.BID, Status.ACTIVE);
            Order ask = new Order("TraderB", ticker, 105.0, 5.0, Side.ASK, Status.ACTIVE);
    
            engine.bidLimitOrder(bid.name, bid);
            engine.askLimitOrder(ask.name, ask);
    
            Map<Double, Deque<Order>> bids = engine.getBids(ticker);
            Map<Double, Deque<Order>> asks = engine.getAsks(ticker);
            assertTrue(bids.containsKey(95.0), "Bids should contain the unmatched bid at 95.0");
            assertTrue(asks.containsKey(105.0), "Asks should contain the unmatched ask at 105.0");
        }
        @Test
        public void testCancelOrderValidBid() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("TraderA", 10000.0);
            Order bid = new Order("TraderA", ticker, 100.0, 10.0, Side.BID, Status.ACTIVE);
    
            // Place and then cancel a bid order
            long orderId = engine.bidLimitOrder(bid.name, bid);
            boolean cancelStatus = engine.removeOrder("TraderA", orderId);
    
            Map<Double, Deque<Order>> bids = engine.getBids(ticker);
            assertTrue(cancelStatus, "Cancel should be successful for a valid bid order");
            //assertFalse(bids.containsKey(100.0), "Bids should not contain the canceled bid at 100.0");
        }
    
        @Test
        public void testCancelOrderValidAsk() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserTickerVolume("TraderB", "AAPL", 100.0);
            engine.initializeUserBalance("TraderB", 10000.0);
            Order ask = new Order("TraderB", ticker, 150.0, 20.0, Side.ASK, Status.ACTIVE);
    
            // Place and then cancel an ask order
            long orderId = engine.askLimitOrder(ask.name, ask);
            boolean cancelStatus = engine.removeOrder("TraderB", orderId);
    
            Map<Double, Deque<Order>> asks = engine.getAsks(ticker);
            assertTrue(cancelStatus, "Cancel should be successful for a valid ask order");
            //assertEquals(Status.CANCELLED, ask.status, "Order should be cancelled");
        }
    
        @Test
        public void testCancelOrderNonExistent() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("TraderA", 1000.0);
            // Attempt to cancel a non-existent order
            boolean cancelStatus = engine.removeOrder("TraderA", 9999); // assuming 9999 is an ID that doesn't exist
            assertFalse(cancelStatus, "Cancel should fail for a non-existent order ID");
        }
    
        @Test
        void testBidPriceLevelsAfterAddingLimitOrder() {
            // Add a bid order
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Trader1", 1000.0);
            Order bidOrder1 = new Order("Trader1", ticker, 100.0, 5.0, Side.BID, Status.ACTIVE);
            engine.bidLimitOrder(bidOrder1.name, bidOrder1);
    
            // Verify the bid price level
            List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
            assertEquals(1, bidLevels.size());
            assertEquals(100.0, bidLevels.get(0).price);
            assertEquals(5.0, bidLevels.get(0).volume);
        }
        @Test
        public void testAskPriceLevelsAfterAddingLimitOrder() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Trader2", 1000.0);
            engine.initializeUserTickerVolume("Trader2", ticker, 3.0);
            Order askOrder1 = new Order("Trader2", ticker, 105.0, 3.0, Side.ASK, Status.ACTIVE);
    
            engine.askLimitOrder(askOrder1.name, askOrder1);
    
            List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
            assertEquals(1, askLevels.size(), "There should be exactly one ask price level");
            assertEquals(105.0, askLevels.get(0).price, "Ask price level should match the inserted order's price");
            assertEquals(3.0, askLevels.get(0).volume, "Ask volume at the level should match the inserted order's volume");
        }
    
        @Test
        public void testPriceLevelsAfterPartialFill() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Trader1", 10000.0);
            engine.initializeUserBalance("Trader2", 10000.0);
            engine.initializeUserTickerVolume("Trader2", "AAPL", 100.0);
            engine.initializeUserTickerVolume("Trader1", ticker, 0.0);
            Order askOrder1 = new Order("Trader2", ticker, 100.0, 5.0, Side.ASK, Status.ACTIVE);
            Order bidOrder1 = new Order("Trader1", ticker, 100.0, 3.0, Side.BID, Status.ACTIVE);
    
            engine.askLimitOrder(askOrder1.name, askOrder1);
            engine.bidLimitOrder(bidOrder1.name, bidOrder1);
    
            List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
            assertEquals(1, askLevels.size(), "There should be one remaining ask price level after partial fill");
            assertEquals(100.0, askLevels.get(0).price, "Ask price level should remain unchanged after partial fill");
            assertEquals(2.0, askLevels.get(0).volume, "Remaining ask volume should be correct after partial fill");
    
            List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
            assertEquals(0, bidLevels.size(), "There should be no bid levels remaining after full fill of the bid order");
        }
    
        @Test
        public void testPriceLevelsAfterFullFill() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Trader1", 1000.0);
            engine.initializeUserBalance("Trader2", 1000.0);
            engine.initializeUserBalance("Trader1", 0.0);
            engine.initializeUserTickerVolume("Trader2", "AAPL", 100.0);
            engine.initializeUserTickerVolume("Trader1", "AAPL", 100.0);
    
            Order askOrder1 = new Order("Trader2", ticker, 100.0, 5.0, Side.ASK, Status.ACTIVE);
            Order bidOrder1 = new Order("Trader1", ticker, 100.0, 5.0, Side.BID, Status.ACTIVE);
    
            engine.askLimitOrder(askOrder1.name, askOrder1);
            engine.bidLimitOrder(bidOrder1.name, bidOrder1);
    
            List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
            assertEquals(0, askLevels.size(), "There should be no remaining ask price levels after full fill");
    
            List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
            assertEquals(0, bidLevels.size(), "There should be no remaining bid price levels after full fill");
        }
    
        @Test
        public void testPriceLevelsWithMultipleOrdersAtSamePrice() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Trader1", 1000.0);
            engine.initializeUserBalance("Trader2", 1000.0);
            engine.initializeUserBalance("Trader3", 1000.0);
            engine.initializeUserTickerVolume("Trader1", "AAPL", 100.0);
            engine.initializeUserTickerVolume("Trader2", "AAPL", 100.0);
            engine.initializeUserTickerVolume("Trader3", "AAPL", 100.0);
    
            Order bidOrder1 = new Order("Trader1", ticker, 100.0, 3.0, Side.BID, Status.ACTIVE);
            Order bidOrder2 = new Order("Trader2", ticker,100.0, 2.0, Side.BID, Status.ACTIVE);
    
            engine.bidLimitOrder(bidOrder1.name, bidOrder1);
            engine.bidLimitOrder(bidOrder2.name, bidOrder2);
    
            List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
            assertEquals(1, bidLevels.size(), "There should be exactly one bid price level");
            assertEquals(100.0, bidLevels.get(0).price, "Bid price level should match the inserted orders' price");
            assertEquals(5.0, bidLevels.get(0).volume, "Bid volume should be aggregated across orders at the same price");
    
            Order askOrder1 = new Order("Trader3", ticker, 100.0, 4.0, Side.ASK, Status.ACTIVE);
            engine.askLimitOrder(askOrder1.name, askOrder1);
    
            bidLevels = engine.getBidPriceLevels(ticker);
            assertEquals(1, bidLevels.size(), "One bid price level should remain after partial match");
            assertEquals(100.0, bidLevels.get(0).price, "Bid price level should remain unchanged");
            assertEquals(1.0, bidLevels.get(0).volume, "Remaining bid volume should be correct after partial fill");
    
            List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
            assertEquals(0, askLevels.size(), "Ask price levels should be empty after full match");
        }
    
        @Test
        public void testRemovingOrderUpdatesPriceLevels() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
    
            engine.initializeUserBalance("Trader1", 1000.0);
            Order bidOrder1 = new Order("Trader1", ticker, 100.0, 3.0, Side.BID, Status.ACTIVE);
    
            long orderId = engine.bidLimitOrder(bidOrder1.name, bidOrder1);
            boolean removed = engine.removeOrder(bidOrder1.name, orderId);
    
            assertTrue(removed, "Order should be successfully removed");
            List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
            assertEquals(0, bidLevels.size(), "Bid price level should be removed after order cancellation");
        }
        @Test
        void testMarketBuyOrderFullyFilled() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
    
            engine.initializeUserBalance("Buyer1", 100000.0);
            engine.initializeUserBalance("Seller1", 100000.0);
            engine.initializeUserBalance("Seller2", 100000.0);
    
            engine.initializeUserTickerVolume("Buyer1", ticker,100000.0);
            engine.initializeUserTickerVolume("Seller1", ticker, 100000.0);
            engine.initializeUserTickerVolume("Seller2", ticker, 100000.0);
            // Setup initial asks
            engine.askLimitOrder("Seller1", new Order("Seller1", ticker, 100.0, 200.0, Side.ASK, Status.ACTIVE));
            engine.askLimitOrder("Seller2", new Order("Seller2", ticker, 101.0, 400.0, Side.ASK, Status.ACTIVE));
    
            // Market Buy Order
            double filledVolume = engine.bidMarketOrder("Buyer1", ticker,500.0);
    
            // Assert filled volume
            assertEquals(500.0, filledVolume);
    
            // Assert remaining asks
            assertEquals(101.0, engine.getLowestAsk(ticker));
            //assertEquals(0.0, engine.getOrder("Seller1", 1).volume);
            //assertTrue(engine.getOrder("Seller1", 1).status == Status.FILLED);
            assertEquals(100.0, engine.getOrder("Seller2", 2).volume);
        }
    
        @Test
        void testMarketSellOrderFullyFilled() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
    
            engine.initializeUserBalance("Buyer1", 100000.0);
            engine.initializeUserBalance("Buyer2", 100000.0);
            engine.initializeUserBalance("Seller1", 10000.0);
            engine.initializeUserTickerVolume("Buyer1", ticker, 500.0);
            engine.initializeUserTickerVolume("Buyer2", ticker, 500.0);
            engine.initializeUserTickerVolume("Seller1", ticker, 500.0);
    
            // Setup initial bids
            engine.bidLimitOrder("Buyer1", new Order("Buyer1", ticker, 99.0, 150.0, Side.BID, Status.ACTIVE));
            engine.bidLimitOrder("Buyer2", new Order("Buyer2", ticker, 98.0, 200.0, Side.BID, Status.ACTIVE));
    
            // Market Sell Order
            double filledVolume = engine.askMarketOrder("Seller1", ticker, 300.0);
    
            // Assert filled volume
            assertEquals(300.0, filledVolume);
    
            // Assert remaining bids
            assertEquals(98.0, engine.getHighestBid(ticker));
            assertEquals(0.0, engine.getOrder("Buyer1", 1).volume);
            assertEquals(50.0, engine.getOrder("Buyer2", 2).volume);
        }
    
        @Test
        void testMarketBuyOrderPartiallyFilled() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Seller1", 1000.0);
            engine.initializeUserBalance("Buyer1", 100000.0);
            engine.initializeUserTickerVolume("Seller1", ticker, 500.0);
            engine.initializeUserTickerVolume("Buyer1", ticker, 0.0);
            // Setup initial asks
            engine.askLimitOrder("Seller1", new Order("Seller1", ticker, 100.0, 200.0, Side.ASK, Status.ACTIVE));
    
            // Market Buy Order
            double filledVolume = engine.bidMarketOrder("Buyer1", ticker, 500.0);
    
            // Assert filled volume
            assertEquals(200.0, filledVolume);
    
            // Assert remaining order state
            assertEquals(Double.POSITIVE_INFINITY, engine.getLowestAsk(ticker));
            assertEquals(0.0, engine.getOrder("Seller1", 1).volume);
            assertEquals(Status.FILLED, engine.getOrder("Seller1", 1).status);
        }
    
        @Test
        void testMarketSellOrderPartiallyFilled() {
            MatchingEngine engine = new MatchingEngine();
            engine.initializeUserBalance("Buyer1", 30000.0);
            engine.initializeUserBalance("Seller1", 30000.0);
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserTickerVolume("Buyer1", ticker, 0.0);
            engine.initializeUserTickerVolume("Seller1", ticker, 1000.0);
    
            // Setup initial bids
            engine.bidLimitOrder("Buyer1", new Order("Buyer1", ticker, 99.0, 300.0, Side.BID, Status.ACTIVE));
    
            // Market Sell Order
            double filledVolume = engine.askMarketOrder("Seller1", ticker, 500.0);
    
            // Assert filled volume
            assertEquals(300.0, filledVolume);
    
            // Assert remaining order state
            assertEquals(0, engine.getHighestBid(ticker));
            assertEquals(0.0, engine.getOrder("Buyer1", 1).volume);
            assertEquals(Status.FILLED, engine.getOrder("Buyer1", 1).status);
            assertEquals(engine.getTickerBalance("Buyer1", ticker), 300.0);
        }
    
        @Test
        void testMarketOrderCancelledDueToNoLiquidity() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
    
            engine.initializeUserBalance("Buyer1", 10000.0);
            engine.initializeUserBalance("Seller1", 10000.0);
    
            // Market Buy Order with no asks
            double buyFilledVolume = engine.bidMarketOrder("Buyer1", ticker, 500.0);
    
            // Market Sell Order with no bids
            double sellFilledVolume = engine.askMarketOrder("Seller1", ticker, 300.0);
    
            // Assert filled volume
            assertEquals(0.0, buyFilledVolume);
            assertEquals(0.0, sellFilledVolume);
        }
        @Test
        void testUninitializedUser() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
    
            long orderId = engine.bidLimitOrder("Buyer1", new Order("Buyer1", ticker, 99.0, 150.0, Side.BID, Status.ACTIVE));
            assertEquals(orderId, -1); // -1 represents invalid user
            orderId = engine.askLimitOrder("RandomName", new Order("RandomName", ticker,5.0, 5.0, Side.ASK, Status.ACTIVE));
            assertEquals(orderId, -1);
            engine.initializeUserBalance("ActualUser", 1000.0);
            engine.initializeUserTickerVolume("ActualUser", ticker, 5.0);
            engine.askLimitOrder("ActualUser", new Order("ActualUser", ticker, 10.0, 5.0, Side.ASK, Status.ACTIVE));
            engine.bidLimitOrder("ActualUser", new Order("ActualUser", ticker, 8.0, 5.0, Side.BID, Status.ACTIVE));
            double orderFilledVolume = engine.bidMarketOrder("Buyer1", ticker, 1000.0);
            assertEquals(orderFilledVolume, 0.0);
            orderFilledVolume = engine.bidMarketOrder("Rasdf;", ticker, 100);
            assertEquals(orderFilledVolume, 0.0);
            assertEquals(engine.getBidPriceLevels(ticker).size(), 1);
            assertEquals(engine.getAskPriceLevels(ticker).size(), 1);
        }
        @Test
        void testUserListLimit() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
    
            engine.initializeUserBalance("Trader1", 1000.0);
            engine.initializeUserBalance("Trader2", 1000.0);
            engine.initializeUserTickerVolume("Trader1", ticker,100.0);
            engine.initializeUserTickerVolume("Trader2", ticker, 100.0);
            assertEquals(engine.getUserBalance("Trader1"), 1000.0);
            Order bidOrder = new Order("Trader1", ticker, 100.0, 5.0, Side.BID, Status.ACTIVE);
            long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
            assertEquals(engine.getUserBalance("Trader1"), 500.0);
            Order askOrder = new Order("Trader2", ticker, 99.0, 5.0, Side.ASK, Status.ACTIVE);
            engine.askLimitOrder("Trader2", askOrder);
            assertEquals(engine.getTickerBalance("Trader1", ticker), 105.0);
            assertEquals(engine.getUserBalance("Trader1"), 500.0);
            assertEquals(engine.getUserBalance("Trader2"), 1500.0);
            //
            Order askOrder2 = new Order("Trader1", ticker, 100.0, 5.0, Side.ASK, Status.ACTIVE);
            engine.askLimitOrder("Trader1", askOrder2);
            assertEquals(engine.getUserBalance("Trader1"), 500.0);
            assertEquals(engine.getUserBalance("Trader2"), 1500.0);
            Order bidOrder2 =  new Order("Trader2", ticker, 102.0, 5.0, Side.BID, Status.ACTIVE);
            engine.bidLimitOrder("Trader2", bidOrder2);
            assertEquals(engine.getUserBalance("Trader1"), 1000.0);
            assertEquals(engine.getUserBalance("Trader2"), 1000.0);
        }
        @Test
        void testCancelUserList() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Trader1", 1000.0);
            Order bidOrder = new Order("Trader1", ticker, 100.0, 5.0, Side.BID, Status.ACTIVE);
            long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
            assertEquals(engine.getUserBalance("Trader1"), 500.0);
            engine.removeOrder("Trader1", orderId);
            assertEquals(engine.getUserBalance("Trader1"), 1000.0);
        }
    
        @Test
        void testUserListMarket() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
    
            engine.initializeUserBalance("Trader1", 1000.0);
            engine.initializeUserBalance("Trader2", 500.0);
            engine.initializeUserTickerVolume("Trader1", ticker, 20.0);
            engine.initializeUserTickerVolume("Trader2", ticker, 20.0);
    
            Order bidOrder = new Order("Trader1", ticker, 100.0, 10.0, Side.BID, Status.ACTIVE);
            long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
            engine.askMarketOrder("Trader2", ticker, 5);
            assertEquals(engine.getUserBalance("Trader1"), 0.0);
            assertEquals(engine.getUserBalance("Trader2"), 1000.0);
            Order askOrder =  new Order("Trader1", ticker, 101.0, 10.0, Side.ASK, Status.ACTIVE);
            orderId = engine.askLimitOrder(askOrder.name, askOrder);
            double volume = engine.bidMarketOrder("Trader2", ticker, 10);
            assertEquals(1000.0 / 101.0, volume);
            assertEquals(1000.0, engine.getUserBalance("Trader1"));
            assertEquals(0.0, engine.getUserBalance("Trader2"));
        }
        @Test
        void testBidTrades() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Trader1", 1000.0);
            Order bidOrder = new Order("Trader1", ticker, 100.0, 10.0, Side.BID, Status.ACTIVE);
            engine.bidLimitOrder(bidOrder.name, bidOrder);
        }
        @Test
        void testBidLimitOrderBalanceUpdates() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Trader1", 1000.0);
    
            Order bidOrder = new Order("Trader1", ticker, 50.0, 10.0, Side.BID, Status.ACTIVE);
            long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
    
            assertEquals(500.0, engine.getUserBalance("Trader1"), "User balance should be reduced by bid order value");
            assertEquals(0.0, engine.getTickerBalance("Trader1", ticker), "Ticker balance should remain unchanged for bid order");
    
            // Remove the order and verify balance restoration
            engine.removeOrder("Trader1", orderId);
            assertEquals(1000.0, engine.getUserBalance("Trader1"), "User balance should be restored after bid order cancellation");
        }
    
        @Test
        void testAskLimitOrderBalanceUpdates() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Trader1", 1000.0);
            engine.initializeUserTickerVolume("Trader1", ticker, 20.0);
    
            Order askOrder = new Order("Trader1", ticker, 50.0, 10.0, Side.ASK, Status.ACTIVE);
            long orderId = engine.askLimitOrder(askOrder.name, askOrder);
    
            assertEquals(1000.0, engine.getUserBalance("Trader1"), "User balance should remain unchanged for ask order");
            assertEquals(10.0, engine.getTickerBalance("Trader1", ticker), "Ticker balance should be reduced by ask order volume");
    
            // Remove the order and verify ticker balance restoration
            engine.removeOrder("Trader1", orderId);
            assertEquals(20.0, engine.getTickerBalance("Trader1", ticker), "Ticker balance should be restored after ask order cancellation");
        }
    
        @Test
        void testBidMarketOrderBalanceUpdates() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
    
            engine.initializeUserBalance("Buyer1", 500.0);
            engine.initializeUserBalance("Seller1", 500.0);
            engine.initializeUserTickerVolume("Seller1", ticker, 10.0);
            engine.initializeUserTickerVolume("Buyer1", ticker, 0.0);
            // Seller posts an ask order
            engine.askLimitOrder("Seller1", new Order("Seller1", ticker, 50.0, 10.0, Side.ASK, Status.ACTIVE));
    
            // Buyer places a market order
            double volumeFilled = engine.bidMarketOrder("Buyer1", ticker, 10.0);
    
            assertEquals(10.0, volumeFilled, "Market order should fully match the ask order volume");
            assertEquals(0.0, engine.getUserBalance("Buyer1"), "Buyer's balance should be fully consumed");
            assertEquals(10.0, engine.getTickerBalance("Buyer1", ticker), "Buyer's ticker balance should increase by matched volume");
            assertEquals(1000.0, engine.getUserBalance("Seller1"), "Seller's balance should increase by the transaction value");
            assertEquals(0.0, engine.getTickerBalance("Seller1", ticker), "Seller's ticker balance should be reduced by the sold volume");
        }
    
        @Test
        void testRemoveAllOrdersRestoresBalances() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker("AAPL");
            engine.initializeUserBalance("Trader1", 1000.0);
            engine.initializeUserTickerVolume("Trader1", ticker, 20.0);
    
            // Place multiple orders
            engine.bidLimitOrder("Trader1", new Order("Trader1", ticker, 50.0, 10.0, Side.BID, Status.ACTIVE));
            engine.askLimitOrder("Trader1", new Order("Trader1", ticker, 60.0, 5.0, Side.ASK, Status.ACTIVE));
    
            // Remove all orders and verify balances
            engine.removeAll("Trader1");
            assertEquals(1000.0, engine.getUserBalance("Trader1"), "User balance should be restored after removing all orders");
            assertEquals(20.0, engine.getTickerBalance("Trader1", ticker), "Ticker balance should be restored after removing all orders");
        }
        @Test
        void testRemoveOrderRestoresAllProperties() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker(ticker);
            engine.initializeUserBalance("Trader1", 1000.0);
            engine.initializeUserTickerVolume("Trader1", ticker, 20.0);
    
            // Place an order
            Order bidOrder = new Order("Trader1", ticker, 100.0, 1, Side.BID, Status.ACTIVE);
            long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
    
            // Verify initial state
            assertEquals(900.0, engine.getUserBalance("Trader1"), "User balance should reflect bid reservation");
            assertEquals(20.0, engine.getTickerBalance("Trader1", ticker), "Ticker volume should remain unchanged for bid orders");
    
            // Remove the order
            boolean removed = engine.removeOrder("Trader1", orderId);
    
            // Verify removal
            assertTrue(removed, "Order should be successfully removed");
            assertEquals(1000.0, engine.getUserBalance("Trader1"), "User balance should be restored after removing bid order");
            assertEquals(20.0, engine.getTickerBalance("Trader1", ticker), "Ticker balance should remain unchanged after removing bid order");
            assertEquals(Status.CANCELLED, engine.getOrder("Trader1", orderId).status, "Order status should be CANCELLED");
        }
    
        @Test
        void testRemoveAllRestoresAllProperties() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker(ticker);
            engine.initializeUserBalance("Trader1", 1000.0);
            engine.initializeUserTickerVolume("Trader1", ticker, 20.0);
    
            // Place multiple orders
            engine.bidLimitOrder("Trader1", new Order("Trader1", ticker, 50.0, 10.0, Side.BID, Status.ACTIVE));
            engine.askLimitOrder("Trader1", new Order("Trader1", ticker, 60.0, 5.0, Side.ASK, Status.ACTIVE));
    
            // Verify initial state
            assertEquals(500.0, engine.getUserBalance("Trader1"), "User balance should reflect bid reservation");
            assertEquals(15.0, engine.getTickerBalance("Trader1", ticker), "Ticker volume should reflect ask reservation");
    
            // Remove all orders
            engine.removeAll("Trader1");
    
            // Verify all properties are restored
            assertEquals(1000.0, engine.getUserBalance("Trader1"), "User balance should be fully restored after removing all orders");
            assertEquals(20.0, engine.getTickerBalance("Trader1", ticker), "Ticker balance should be fully restored after removing all orders");
            List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
            List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
            assertEquals(0, bidLevels.size(), "All bid orders should be removed");
            assertEquals(0, askLevels.size(), "All ask orders should be removed");
        }
        @Test
        void testRemoveOrderForAskRestoresAllProperties() {
            MatchingEngine engine = new MatchingEngine();
            String ticker = "AAPL";
            engine.initializeTicker(ticker);
            engine.initializeUserBalance("Trader1", 1000.0);
            engine.initializeUserTickerVolume("Trader1", ticker, 20.0);
    
            // Place an ask order
            Order askOrder = new Order("Trader1", ticker, 100.0, 10.0, Side.ASK, Status.ACTIVE);
            long orderId = engine.askLimitOrder(askOrder.name, askOrder);
    
            // Verify initial state
            assertEquals(1000.0, engine.getUserBalance("Trader1"), "User balance should remain unchanged for ask orders");
            assertEquals(10.0, engine.getTickerBalance("Trader1", ticker), "Ticker balance should reflect ask reservation");
    
            // Remove the ask order
            boolean removed = engine.removeOrder("Trader1", orderId);
    
            // Verify removal
            assertTrue(removed, "Ask order should be successfully removed");
            assertEquals(1000.0, engine.getUserBalance("Trader1"), "User balance should remain unchanged after removing ask order");
            assertEquals(20.0, engine.getTickerBalance("Trader1", ticker), "Ticker balance should be fully restored after removing ask order");
            assertEquals(Status.CANCELLED, engine.getOrder("Trader1", orderId).status, "Order status should be CANCELLED");
            List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
            assertEquals(0, askLevels.size(), "Ask price levels should be empty after removing the order");
        }
        @Test
        void initializeAllTickers() {
            MatchingEngine matchingEngine = new MatchingEngine();
            matchingEngine.initializeAllTickers();
        }
        @Test
        void initializeAllUsers() {
            MatchingEngine matchingEngine = new MatchingEngine();
            matchingEngine.initializeUser("Trader1");
        }
        @Test
        public void testAddTradeAndCheckVolumeSummation() {
            MatchingEngine engine = new MatchingEngine();
            RecentTrades.getRecentTrades();
            String ticker = "AAPL";
            Map<Double, Double> volumeMap = new TreeMap<>();
            Side side = Side.BID;
    
            engine.updateVolume(volumeMap, 100.0, 10.0, ticker, side);
            engine.updateVolume(volumeMap, 100.0, 5.0, ticker, side);
    
            ArrayList<PriceChange> trades = RecentTrades.getRecentTrades();
            assertFalse(trades.isEmpty(), "Trades list should not be empty");
            assertEquals(1, trades.size(), "There should be exactly one trade recorded");
    
            PriceChange trade = trades.get(0);
            assertEquals(100.0, trade.getPrice(), "Trade price should match the updated price");
            assertEquals(15.0, trade.getVolume(), "Trade volume should be the sum of updates");
            assertEquals(side, trade.getSide(), "Trade side should match the updated side");
        }
    
        @Test
        public void testTradeLoggingForDifferentPrices() {
            MatchingEngine engine = new MatchingEngine();
            RecentTrades.getRecentTrades();
            String ticker = "AAPL";
            Map<Double, Double> volumeMap = new TreeMap<>();
            Side side = Side.ASK;
    
            engine.updateVolume(volumeMap, 100.0, 10.0, ticker, side);
            engine.updateVolume(volumeMap, 105.0, 20.0, ticker, side);
    
            ArrayList<PriceChange> trades = RecentTrades.getRecentTrades();
            assertEquals(2, trades.size(), "Should log trades at two different prices");
    
            // Checking each trade
            PriceChange firstTrade = trades.get(0);
            PriceChange secondTrade = trades.get(1);
    
            if (firstTrade.getPrice() == 100.0) {
                assertEquals(10.0, firstTrade.getVolume(), "Volume at price 100.0 should be 10.0");
                assertEquals(105.0, secondTrade.getPrice(), "Second trade price should be 105.0");
                assertEquals(20.0, secondTrade.getVolume(), "Volume at price 105.0 should be 20.0");
            } else {
                assertEquals(20.0, firstTrade.getVolume(), "Volume at price 105.0 should be 20.0");
                assertEquals(100.0, secondTrade.getPrice(), "Second trade price should be 100.0");
                assertEquals(10.0, secondTrade.getVolume(), "Volume at price 100.0 should be 10.0");
            }
        }
    
        @Test
        public void testTradeClearingPostRetrieval() {
            MatchingEngine engine = new MatchingEngine();
            RecentTrades.getRecentTrades();
            String ticker = "AAPL";
            Map<Double, Double> volumeMap = new TreeMap<>();
            Side side = Side.BID;
    
            engine.updateVolume(volumeMap, 100.0, 10.0, ticker, side);
    
            // Get and clear trades
            assertNotNull(RecentTrades.getRecentTrades(), "Should return a non-null list of trades");
            assertTrue(RecentTrades.getRecentTrades().isEmpty(), "Trades should be cleared after retrieval");
        }
        @Test
        public void testLimitOrderTradeLogging() {
            MatchingEngine engine = new MatchingEngine();
            RecentTrades.getRecentTrades();
            String ticker = "GOOG";
            engine.initializeTicker(ticker);
            engine.initializeUserBalance("TraderC", 2000.0);
            engine.initializeUserTickerVolume("TraderC", ticker, 50.0);

            // Place a bid limit order and an ask limit order that should match
            engine.bidLimitOrder("TraderC", new Order("TraderC", ticker, 150.0, 5.0, Side.BID, Status.ACTIVE));
            engine.askLimitOrder("TraderC", new Order("TraderC", ticker, 150.0, 5.0, Side.ASK, Status.ACTIVE));

            // Check trades logged
            List<PriceChange> trades = RecentTrades.getRecentTrades();
            assertFalse(trades.isEmpty(), "Trades should be logged for matching orders");
            assertEquals(1, trades.size(), "One trade should be logged for the matched orders");

            PriceChange trade = trades.get(0);
            assertEquals(150.0, trade.getPrice(), "Trade price should match the order price");
            assertEquals(0.0, trade.getVolume(), "Trade volume should match the order volume");
            assertNotNull(trade.getSide(), "Trade should have a side");
        }

        @Test
        public void testMarketOrderTradeLogging() {
            MatchingEngine engine = new MatchingEngine();
            RecentTrades.getRecentTrades();
            String ticker = "GOOG";
            engine.initializeTicker(ticker);
            engine.initializeUserBalance("TraderD", 5000.0);
            engine.initializeUserTickerVolume("TraderD", ticker, 100.0);

            // Set up limit orders to be matched by a market order
            engine.askLimitOrder("TraderD", new Order("TraderD", ticker, 200.0, 20.0, Side.ASK, Status.ACTIVE));
            engine.askLimitOrder("TraderD", new Order("TraderD", ticker, 205.0, 20.0, Side.ASK, Status.ACTIVE));
            List<PriceChange> trades = RecentTrades.getRecentTrades();
            assertEquals(2, trades.size());
            // Place market buy order that matches the above asks
            engine.bidMarketOrder("TraderD", ticker, 30.0);

            // Check trades logged
            trades = RecentTrades.getRecentTrades();
            assertEquals(2, trades.size(), "Two trades should be logged for the matched market order");

            // Check first trade details
            PriceChange firstTrade = trades.get(0);
            assertEquals(200.0, firstTrade.getPrice(), "First trade price should be 200.0");
            assertEquals(-20.0, firstTrade.getVolume(), "First trade volume should be 20.0");

            // Check second trade details
            PriceChange secondTrade = trades.get(1);
            assertEquals(205.0, secondTrade.getPrice(), "Second trade price should be 205.0");
            assertEquals(-10.0, secondTrade.getVolume(), "Second trade volume should be 10.0");
        }

        @Test
        public void testCancelOrderTradeLogging() {
            MatchingEngine engine = new MatchingEngine();
            RecentTrades.getRecentTrades();
            String ticker = "GOOG";
            engine.initializeTicker(ticker);
            engine.initializeUserBalance("TraderE", 10000.0);
            engine.initializeUserTickerVolume("TraderE", ticker, 50.0);

            // Place and cancel a bid limit order
            long orderId = engine.bidLimitOrder("TraderE", new Order("TraderE", ticker, 250.0, 10.0, Side.BID, Status.ACTIVE));
            engine.removeOrder("TraderE", orderId);

            // Check that no trades were logged for the cancellation
            List<PriceChange> trades = RecentTrades.getRecentTrades();
            System.out.println(trades);
            assertEquals(trades.size(),1, "Cancel is also in the map");
        }
        private static final double POSITION_LIMIT = 100.0;
        @Test
        public void testValidTransactionsWithinPositionLimits() {
            MatchingEngine engine = new MatchingEngine(POSITION_LIMIT);
            String user = "TraderZ";
            String ticker = "AAPL";
            engine.initializeUserBalance(user, 0.0);
            engine.initializeTicker(ticker);
            engine.initializeUserTickerVolume(user, ticker, 50.0); // Long 50 shares

            // Try to buy up to position limit
            System.out.println("This Far");
            Order buyOrder = new Order(user, ticker, 150.0, 50.0, Side.BID, Status.ACTIVE); // Buy additional 50 to reach 100
            long orderId = engine.bidLimitOrder(user, buyOrder);
            System.out.println(orderId);
            assertTrue(orderId > 0, "Should allow buying within position limit");

            // Try to short up to position limit
            String user2 = "Trader2";
            engine.initializeUserBalance(user2, 0.0);
            engine.initializeUserTickerVolume(user, ticker, 50.0); // Short 50 shares
            Order sellOrder = new Order(user, ticker, 150.0, 150.0, Side.ASK, Status.ACTIVE); // Short additional 50 to reach -100
            orderId = engine.askLimitOrder(user, sellOrder);
            assertTrue(orderId > 0, "Should allow shorting within position limit");
        }

        @Test
        public void testHandlingNegativeQuantities() {
            MatchingEngine engine = new MatchingEngine(POSITION_LIMIT);
            String user = "TraderW";
            String user2 = "TraderX";
            String ticker = "AAPL";

            engine.initializeTicker(ticker);
            engine.initializeUserBalance(user, 0.0);
            engine.initializeUserBalance(user2, 0.0);
            engine.initializeUserTickerVolume(user2, ticker, 0.0);
            // Short sell within limits
            engine.initializeUserTickerVolume(user, ticker, -50.0);
            double position = engine.getTickerBalance(user, ticker);
            assertEquals(-50.0, position, "Should correctly handle short positions");

            // Cover short within limits
            Order buyOrder = new Order(user, ticker, 150.0, 50.0, Side.BID, Status.ACTIVE);
            engine.bidLimitOrder(user, buyOrder);
            Order askOrder = new Order(user2, ticker, 145.0, 50.0, Side.ASK, Status.ACTIVE);
            position = engine.getTickerBalance(user, ticker);
            double position2 = engine.getTickerBalance(user2, ticker);

        }

        @Test
        public void testRejectionOfTradesThatBreachPositionLimits() {
            MatchingEngine engine = new MatchingEngine(POSITION_LIMIT);
            String user = "TraderV";
            String ticker = "AAPL";

            engine.initializeTicker(ticker);
            engine.initializeUserBalance(user, 0.0);
            engine.initializeUserTickerVolume(user, ticker, 90.0); // Long 90 shares

            // Attempt to buy over the position limit
            Order buyOrder = new Order(user, ticker, 200.0, 20.0, Side.BID, Status.ACTIVE);
            long orderId = engine.bidLimitOrder(user, buyOrder);
            assertEquals(-1, orderId, "Should reject buying that breaches position limit");

            // Attempt to short over the position limit
            //engine.initializeTicker(ticker);
            engine.initializeUserTickerVolume(user, ticker, -90.0); // Short 90 shares
            Order sellOrder = new Order(user, ticker, 200.0, 20.0, Side.ASK, Status.ACTIVE);
            orderId = engine.askLimitOrder(user, sellOrder);
            assertEquals(-1, orderId, "Should reject shorting that breaches position limit");
        }

    }
