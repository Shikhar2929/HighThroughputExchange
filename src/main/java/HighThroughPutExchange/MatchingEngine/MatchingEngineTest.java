package HighThroughPutExchange.MatchingEngine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

public class MatchingEngineTest {

    private static final double TOLERANCE = 1e-6;

    private boolean almostEqual(double a, double b) {
        return Math.abs(a - b) < TOLERANCE;
    }

    /*
     * @Test public void testBidLimitOrder_AddsBidSuccessfully() { MatchingEngine
     * engine = new MatchingEngine(); engine.initializeUserBalance("TraderA", 1000);
     * String ticker = "AAPL"; engine.initializeTicker(ticker);
     * engine.initializeUserTickerVolume("TraderA", ticker, 10); Order bidOrder =
     * new Order("TraderA", ticker,100, 10, Side.BID, Status.ACTIVE);
     *
     * long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
     * assertTrue(orderId > 0, "Order ID should be valid and greater than 0");
     *
     * Map<Double, Deque<Order>> bids = engine.getBids(ticker);
     * assertTrue(bids.containsKey(bidOrder.price),
     * "Bid map should contain the order price level");
     * assertEquals(bidOrder.volume, bids.get(bidOrder.price).peek().volume,
     * "Bid volume should match"); assertEquals(bidOrder.name,
     * bids.get(bidOrder.price).peek().name, "Bid name should match"); }
     *
     * @Test public void testAskLimitOrder_AddsAskSuccessfully() { MatchingEngine
     * engine = new MatchingEngine();
     *
     * engine.initializeUserBalance("TraderB", 10000); String ticker = "AAPL";
     * engine.initializeTicker(ticker); engine.initializeUserTickerVolume("TraderB",
     * ticker, 105); Order askOrder = new Order("TraderB", ticker, 105, 15,
     * Side.ASK, Status.ACTIVE);
     *
     * long orderId = engine.askLimitOrder(askOrder.name, askOrder);
     * assertTrue(orderId > 0, "Order ID should be valid and greater than 0");
     *
     * Map<Double, Deque<Order>> asks = engine.getAsks(ticker);
     * assertTrue(asks.containsKey(askOrder.price),
     * "Ask map should contain the order price level");
     * assertEquals(askOrder.volume, asks.get(askOrder.price).peek().volume,
     * "Ask volume should match"); assertEquals(askOrder.name,
     * asks.get(askOrder.price).peek().name, "Ask name should match"); }
     *
     * @Test public void testGetHighestBid_AfterMultipleBids() { MatchingEngine
     * engine = new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker(ticker); engine.initializeUserBalance("TraderA",
     * 10000); engine.initializeUserBalance("TraderB", 10000);
     * engine.bidLimitOrder("TraderA", new Order("TraderA", ticker, 100, 10,
     * Side.BID, Status.ACTIVE)); engine.bidLimitOrder("TraderB", new
     * Order("TraderB", ticker, 105, 5, Side.BID, Status.ACTIVE));
     *
     * assertTrue(almostEqual(engine.getHighestBid(ticker), 105),
     * "Highest bid should be 105"); }
     *
     * @Test public void testGetLowestAsk_AfterMultipleAsks() { MatchingEngine
     * engine = new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker("AAPL"); engine.initializeUserBalance("TraderA",
     * 10000); engine.initializeUserBalance("TraderB", 10000);
     * engine.initializeUserTickerVolume("TraderA", ticker, 10);
     * engine.initializeUserTickerVolume("TraderB", ticker, 10);
     *
     * engine.askLimitOrder("TraderA", new Order("TraderA", ticker, 110, 10,
     * Side.ASK, Status.ACTIVE)); engine.askLimitOrder("TraderB", new
     * Order("TraderB", ticker, 105, 5, Side.ASK, Status.ACTIVE));
     * System.out.println(engine.getLowestAsk(ticker));
     * assertTrue(almostEqual(engine.getLowestAsk(ticker), 105),
     * "Lowest ask should be 105"); }
     *
     * @Test public void testMatchingBidAndAskOrders() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("TraderA", 30000);
     * engine.initializeUserBalance("TraderB", 30000);
     * engine.initializeUserTickerVolume("TraderA", ticker, 0);
     * engine.initializeUserTickerVolume("TraderB", ticker, 10);
     * engine.bidLimitOrder("TraderA", new Order("TraderA", ticker, 105, 10,
     * Side.BID, Status.ACTIVE)); engine.askLimitOrder("TraderB", new
     * Order("TraderB", ticker,105, 10, Side.ASK, Status.ACTIVE));
     *
     * assertTrue(almostEqual(engine.getHighestBid(ticker), 0),
     * "Highest bid should be 0 after matching");
     * System.out.println(engine.getLowestAsk(ticker));
     * assertEquals(engine.getLowestAsk(ticker), Double.POSITIVE_INFINITY,
     * "Lowest ask should be 0 after matching"); }
     *
     * @Test public void testInsertBid() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("TraderA", 1000); Order bidOrder = new
     * Order("TraderA", ticker, 100, 10, Side.BID, Status.ACTIVE);
     *
     * long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
     * assertTrue(orderId > 0, "Order ID should be valid and greater than 0");
     *
     * Map<Double, Deque<Order>> bids = engine.getBids(ticker);
     * assertTrue(bids.containsKey(100),
     * "Bids should contain the inserted order price"); assertEquals(10,
     * bids.get(100).peek().volume, "Bid volume should be 10"); }
     *
     * @Test public void testInsertAsk() { String ticker = "AAPL"; MatchingEngine
     * engine = new MatchingEngine(); engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("TraderB", 1000);
     * engine.initializeUserTickerVolume("TraderB", ticker, 10); Order askOrder =
     * new Order("TraderB", ticker,100, 5, Side.ASK, Status.ACTIVE);
     *
     * long orderId = engine.askLimitOrder(askOrder.name, askOrder);
     * assertTrue(orderId > 0, "Order ID should be valid and greater than 0");
     *
     * Map<Double, Deque<Order>> asks = engine.getAsks(ticker);
     * assertTrue(asks.containsKey(100),
     * "Asks should contain the inserted order price"); assertEquals(5,
     * asks.get(100).peek().volume, "Ask volume should be 5"); }
     *
     * @Test public void testFillOrderCompletely() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("TraderA", 10000);
     * engine.initializeUserBalance("TraderB", 10000);
     * engine.initializeUserTickerVolume("TraderA", "AAPL", 5);
     * engine.initializeUserTickerVolume("TraderB", ticker, 0); Order askOrder = new
     * Order("TraderA", ticker, 100, 5, Side.ASK, Status.ACTIVE); Order bidOrder =
     * new Order("TraderB", ticker, 100, 5, Side.BID, Status.ACTIVE);
     *
     * engine.askLimitOrder(askOrder.name, askOrder);
     * engine.bidLimitOrder(bidOrder.name, bidOrder);
     *
     * assertTrue(engine.getAsks(ticker).isEmpty(),
     * "Asks should be empty after full match");
     * assertTrue(engine.getBids(ticker).isEmpty(),
     * "Bids should be empty after full match"); }
     *
     * @Test public void testPartialFill() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("TraderA", 1000);
     * engine.initializeUserBalance("TraderB", 1000);
     * engine.initializeUserTickerVolume("TraderA", ticker, 5);
     * engine.initializeUserTickerVolume("TraderB", ticker, 0); Order askOrder = new
     * Order("TraderA", ticker, 100, 5, Side.ASK, Status.ACTIVE); Order bidOrder =
     * new Order("TraderB", ticker, 100, 10, Side.BID, Status.ACTIVE);
     *
     * engine.askLimitOrder(askOrder.name, askOrder);
     * engine.bidLimitOrder(bidOrder.name, bidOrder);
     *
     * Map<Double, Deque<Order>> bids = engine.getBids(ticker);
     * assertTrue(engine.getAsks(ticker).isEmpty(),
     * "Asks should be empty after partial match");
     * assertTrue(bids.containsKey(100),
     * "Bids should contain the remaining bid at 100"); assertEquals(5,
     * bids.get(100).peek().volume,
     * "Remaining bid volume should be 5 after partial fill"); }
     *
     * @Test public void testRaceCondition() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("TraderA", 1000);
     * engine.initializeUserBalance("TraderB", 1000);
     * engine.initializeUserBalance("TraderC", 1000);
     * engine.initializeUserTickerVolume("TraderA", ticker, 15);
     * engine.initializeUserTickerVolume("TraderB", ticker, 15);
     * engine.initializeUserTickerVolume("TraderC", ticker, 15);
     *
     * Order ask1 = new Order("TraderA", ticker, 100, 5, Side.ASK, Status.ACTIVE);
     * Order ask2 = new Order("TraderB", ticker, 100, 7, Side.ASK, Status.ACTIVE);
     * Order bid = new Order("TraderC", ticker, 100, 6, Side.BID, Status.ACTIVE);
     *
     * engine.askLimitOrder(ask1.name, ask1); engine.askLimitOrder(ask2.name, ask2);
     * engine.bidLimitOrder(bid.name, bid);
     *
     * Map<Double, Deque<Order>> asks = engine.getAsks(ticker);
     * assertTrue(asks.containsKey(100),
     * "Asks should contain the remaining volume at 100"); assertEquals(6,
     * asks.get(100).peek().volume,
     * "Remaining ask volume should be 6 after partial fill"); }
     *
     * @Test public void testDifferentPricesNoMatch() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("TraderA", 10000);
     * engine.initializeUserBalance("TraderB", 10000);
     * engine.initializeUserTickerVolume("TraderA", ticker, 10);
     * engine.initializeUserTickerVolume("TraderB", ticker, 10); Order bid = new
     * Order("TraderA", ticker, 95, 10, Side.BID, Status.ACTIVE); Order ask = new
     * Order("TraderB", ticker, 105, 5, Side.ASK, Status.ACTIVE);
     *
     * engine.bidLimitOrder(bid.name, bid); engine.askLimitOrder(ask.name, ask);
     *
     * Map<Double, Deque<Order>> bids = engine.getBids(ticker); Map<Double,
     * Deque<Order>> asks = engine.getAsks(ticker); assertTrue(bids.containsKey(95),
     * "Bids should contain the unmatched bid at 95");
     * assertTrue(asks.containsKey(105),
     * "Asks should contain the unmatched ask at 105"); }
     *
     * @Test public void testCancelOrderValidBid() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("TraderA", 10000); Order bid = new
     * Order("TraderA", ticker, 100, 10, Side.BID, Status.ACTIVE);
     *
     * // Place and then cancel a bid order long orderId =
     * engine.bidLimitOrder(bid.name, bid); boolean cancelStatus =
     * engine.removeOrder("TraderA", orderId);
     *
     * Map<Double, Deque<Order>> bids = engine.getBids(ticker);
     * assertTrue(cancelStatus,
     * "Cancel should be successful for a valid bid order");
     * //assertFalse(bids.containsKey(100),
     * "Bids should not contain the canceled bid at 100"); }
     *
     * @Test public void testCancelOrderValidAsk() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserTickerVolume("TraderB", "AAPL", 100);
     * engine.initializeUserBalance("TraderB", 10000); Order ask = new
     * Order("TraderB", ticker, 150, 20, Side.ASK, Status.ACTIVE);
     *
     * // Place and then cancel an ask order long orderId =
     * engine.askLimitOrder(ask.name, ask); boolean cancelStatus =
     * engine.removeOrder("TraderB", orderId);
     *
     * Map<Double, Deque<Order>> asks = engine.getAsks(ticker);
     * assertTrue(cancelStatus,
     * "Cancel should be successful for a valid ask order");
     * //assertEquals(Status.CANCELLED, ask.status, "Order should be cancelled"); }
     *
     * @Test public void testCancelOrderNonExistent() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("TraderA", 1000); // Attempt to cancel a
     * non-existent order boolean cancelStatus = engine.removeOrder("TraderA",
     * 9999); // assuming 9999 is an ID that doesn't exist assertFalse(cancelStatus,
     * "Cancel should fail for a non-existent order ID"); }
     *
     * @Test void testBidPriceLevelsAfterAddingLimitOrder() { // Add a bid order
     * MatchingEngine engine = new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker("AAPL"); engine.initializeUserBalance("Trader1",
     * 1000); Order bidOrder1 = new Order("Trader1", ticker, 100, 5, Side.BID,
     * Status.ACTIVE); engine.bidLimitOrder(bidOrder1.name, bidOrder1);
     *
     * // Verify the bid price level List<PriceLevel> bidLevels =
     * engine.getBidPriceLevels(ticker); assertEquals(1, bidLevels.size());
     * assertEquals(100, bidLevels.get(0).price); assertEquals(5,
     * bidLevels.get(0).volume); }
     *
     * @Test public void testAskPriceLevelsAfterAddingLimitOrder() { MatchingEngine
     * engine = new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker("AAPL"); engine.initializeUserBalance("Trader2",
     * 1000); engine.initializeUserTickerVolume("Trader2", ticker, 3); Order
     * askOrder1 = new Order("Trader2", ticker, 105, 3, Side.ASK, Status.ACTIVE);
     *
     * engine.askLimitOrder(askOrder1.name, askOrder1);
     *
     * List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
     * assertEquals(1, askLevels.size(),
     * "There should be exactly one ask price level"); assertEquals(105,
     * askLevels.get(0).price,
     * "Ask price level should match the inserted order's price"); assertEquals(3,
     * askLevels.get(0).volume,
     * "Ask volume at the level should match the inserted order's volume"); }
     *
     * @Test public void testPriceLevelsAfterPartialFill() { MatchingEngine engine =
     * new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker("AAPL"); engine.initializeUserBalance("Trader1",
     * 10000); engine.initializeUserBalance("Trader2", 10000);
     * engine.initializeUserTickerVolume("Trader2", "AAPL", 100);
     * engine.initializeUserTickerVolume("Trader1", ticker, 0); Order askOrder1 =
     * new Order("Trader2", ticker, 100, 5, Side.ASK, Status.ACTIVE); Order
     * bidOrder1 = new Order("Trader1", ticker, 100, 3, Side.BID, Status.ACTIVE);
     *
     * engine.askLimitOrder(askOrder1.name, askOrder1);
     * engine.bidLimitOrder(bidOrder1.name, bidOrder1);
     *
     * List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
     * assertEquals(1, askLevels.size(),
     * "There should be one remaining ask price level after partial fill");
     * assertEquals(100, askLevels.get(0).price,
     * "Ask price level should remain unchanged after partial fill");
     * assertEquals(2, askLevels.get(0).volume,
     * "Remaining ask volume should be correct after partial fill");
     *
     * List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
     * assertEquals(0, bidLevels.size(),
     * "There should be no bid levels remaining after full fill of the bid order");
     * }
     *
     * @Test public void testPriceLevelsAfterFullFill() { MatchingEngine engine =
     * new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker("AAPL"); engine.initializeUserBalance("Trader1",
     * 1000); engine.initializeUserBalance("Trader2", 1000);
     * engine.initializeUserBalance("Trader1", 0);
     * engine.initializeUserTickerVolume("Trader2", "AAPL", 100);
     * engine.initializeUserTickerVolume("Trader1", "AAPL", 100);
     *
     * Order askOrder1 = new Order("Trader2", ticker, 100, 5, Side.ASK,
     * Status.ACTIVE); Order bidOrder1 = new Order("Trader1", ticker, 100, 5,
     * Side.BID, Status.ACTIVE);
     *
     * engine.askLimitOrder(askOrder1.name, askOrder1);
     * engine.bidLimitOrder(bidOrder1.name, bidOrder1);
     *
     * List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
     * assertEquals(0, askLevels.size(),
     * "There should be no remaining ask price levels after full fill");
     *
     * List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
     * assertEquals(0, bidLevels.size(),
     * "There should be no remaining bid price levels after full fill"); }
     *
     * @Test public void testPriceLevelsWithMultipleOrdersAtSamePrice() {
     * MatchingEngine engine = new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker("AAPL"); engine.initializeUserBalance("Trader1",
     * 1000); engine.initializeUserBalance("Trader2", 1000);
     * engine.initializeUserBalance("Trader3", 1000);
     * engine.initializeUserTickerVolume("Trader1", "AAPL", 100);
     * engine.initializeUserTickerVolume("Trader2", "AAPL", 100);
     * engine.initializeUserTickerVolume("Trader3", "AAPL", 100);
     *
     * Order bidOrder1 = new Order("Trader1", ticker, 100, 3, Side.BID,
     * Status.ACTIVE); Order bidOrder2 = new Order("Trader2", ticker,100, 2,
     * Side.BID, Status.ACTIVE);
     *
     * engine.bidLimitOrder(bidOrder1.name, bidOrder1);
     * engine.bidLimitOrder(bidOrder2.name, bidOrder2);
     *
     * List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
     * assertEquals(1, bidLevels.size(),
     * "There should be exactly one bid price level"); assertEquals(100,
     * bidLevels.get(0).price,
     * "Bid price level should match the inserted orders' price"); assertEquals(5,
     * bidLevels.get(0).volume,
     * "Bid volume should be aggregated across orders at the same price");
     *
     * Order askOrder1 = new Order("Trader3", ticker, 100, 4, Side.ASK,
     * Status.ACTIVE); engine.askLimitOrder(askOrder1.name, askOrder1);
     *
     * bidLevels = engine.getBidPriceLevels(ticker); assertEquals(1,
     * bidLevels.size(), "One bid price level should remain after partial match");
     * assertEquals(100, bidLevels.get(0).price,
     * "Bid price level should remain unchanged"); assertEquals(1,
     * bidLevels.get(0).volume,
     * "Remaining bid volume should be correct after partial fill");
     *
     * List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
     * assertEquals(0, askLevels.size(),
     * "Ask price levels should be empty after full match"); }
     *
     * @Test public void testRemovingOrderUpdatesPriceLevels() { MatchingEngine
     * engine = new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker("AAPL");
     *
     * engine.initializeUserBalance("Trader1", 1000); Order bidOrder1 = new
     * Order("Trader1", ticker, 100, 3, Side.BID, Status.ACTIVE);
     *
     * long orderId = engine.bidLimitOrder(bidOrder1.name, bidOrder1); boolean
     * removed = engine.removeOrder(bidOrder1.name, orderId);
     *
     * assertTrue(removed, "Order should be successfully removed"); List<PriceLevel>
     * bidLevels = engine.getBidPriceLevels(ticker); assertEquals(0,
     * bidLevels.size(),
     * "Bid price level should be removed after order cancellation"); }
     *
     * @Test void testMarketBuyOrderFullyFilled() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     *
     * engine.initializeUserBalance("Buyer1", 100000);
     * engine.initializeUserBalance("Seller1", 100000);
     * engine.initializeUserBalance("Seller2", 100000);
     *
     * engine.initializeUserTickerVolume("Buyer1", ticker,100000);
     * engine.initializeUserTickerVolume("Seller1", ticker, 100000);
     * engine.initializeUserTickerVolume("Seller2", ticker, 100000); // Setup
     * initial asks engine.askLimitOrder("Seller1", new Order("Seller1", ticker,
     * 100, 200, Side.ASK, Status.ACTIVE)); engine.askLimitOrder("Seller2", new
     * Order("Seller2", ticker, 101, 400, Side.ASK, Status.ACTIVE));
     *
     * // Market Buy Order double filledVolume = engine.bidMarketOrder("Buyer1",
     * ticker,500);
     *
     * // Assert filled volume assertEquals(500, filledVolume);
     *
     * // Assert remaining asks assertEquals(101, engine.getLowestAsk(ticker));
     * //assertEquals(0, engine.getOrder("Seller1", 1).volume);
     * //assertTrue(engine.getOrder("Seller1", 1).status == Status.FILLED);
     * assertEquals(100, engine.getOrder("Seller2", 2).volume); }
     *
     * @Test void testMarketSellOrderFullyFilled() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     *
     * engine.initializeUserBalance("Buyer1", 100000);
     * engine.initializeUserBalance("Buyer2", 100000);
     * engine.initializeUserBalance("Seller1", 10000);
     * engine.initializeUserTickerVolume("Buyer1", ticker, 500);
     * engine.initializeUserTickerVolume("Buyer2", ticker, 500);
     * engine.initializeUserTickerVolume("Seller1", ticker, 500);
     *
     * // Setup initial bids engine.bidLimitOrder("Buyer1", new Order("Buyer1",
     * ticker, 99, 150, Side.BID, Status.ACTIVE)); engine.bidLimitOrder("Buyer2",
     * new Order("Buyer2", ticker, 98, 200, Side.BID, Status.ACTIVE));
     *
     * // Market Sell Order double filledVolume = engine.askMarketOrder("Seller1",
     * ticker, 300);
     *
     * // Assert filled volume assertEquals(300, filledVolume);
     *
     * // Assert remaining bids assertEquals(98, engine.getHighestBid(ticker));
     * assertEquals(0, engine.getOrder("Buyer1", 1).volume); assertEquals(50,
     * engine.getOrder("Buyer2", 2).volume); }
     *
     * @Test void testMarketBuyOrderPartiallyFilled() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("Seller1", 1000);
     * engine.initializeUserBalance("Buyer1", 100000);
     * engine.initializeUserTickerVolume("Seller1", ticker, 500);
     * engine.initializeUserTickerVolume("Buyer1", ticker, 0); // Setup initial asks
     * engine.askLimitOrder("Seller1", new Order("Seller1", ticker, 100, 200,
     * Side.ASK, Status.ACTIVE));
     *
     * // Market Buy Order double filledVolume = engine.bidMarketOrder("Buyer1",
     * ticker, 500);
     *
     * // Assert filled volume assertEquals(200, filledVolume);
     *
     * // Assert remaining order state assertEquals(Double.POSITIVE_INFINITY,
     * engine.getLowestAsk(ticker)); assertEquals(0, engine.getOrder("Seller1",
     * 1).volume); assertEquals(Status.FILLED, engine.getOrder("Seller1",
     * 1).status); }
     *
     * @Test void testMarketSellOrderPartiallyFilled() { MatchingEngine engine = new
     * MatchingEngine(); engine.initializeUserBalance("Buyer1", 30000);
     * engine.initializeUserBalance("Seller1", 30000); String ticker = "AAPL";
     * engine.initializeTicker("AAPL"); engine.initializeUserTickerVolume("Buyer1",
     * ticker, 0); engine.initializeUserTickerVolume("Seller1", ticker, 1000);
     *
     * // Setup initial bids engine.bidLimitOrder("Buyer1", new Order("Buyer1",
     * ticker, 99, 300, Side.BID, Status.ACTIVE));
     *
     * // Market Sell Order double filledVolume = engine.askMarketOrder("Seller1",
     * ticker, 500);
     *
     * // Assert filled volume assertEquals(300, filledVolume);
     *
     * // Assert remaining order state assertEquals(0,
     * engine.getHighestBid(ticker)); assertEquals(0, engine.getOrder("Buyer1",
     * 1).volume); assertEquals(Status.FILLED, engine.getOrder("Buyer1", 1).status);
     * assertEquals(engine.getTickerBalance("Buyer1", ticker), 300); }
     *
     * @Test void testMarketOrderCancelledDueToNoLiquidity() { MatchingEngine engine
     * = new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker("AAPL");
     *
     * engine.initializeUserBalance("Buyer1", 10000);
     * engine.initializeUserBalance("Seller1", 10000);
     *
     * // Market Buy Order with no asks double buyFilledVolume =
     * engine.bidMarketOrder("Buyer1", ticker, 500);
     *
     * // Market Sell Order with no bids double sellFilledVolume =
     * engine.askMarketOrder("Seller1", ticker, 300);
     *
     * // Assert filled volume assertEquals(0, buyFilledVolume); assertEquals(0,
     * sellFilledVolume); }
     *
     * @Test void testUninitializedUser() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     *
     * long orderId = engine.bidLimitOrder("Buyer1", new Order("Buyer1", ticker, 99,
     * 150, Side.BID, Status.ACTIVE)); assertEquals(orderId, -1); // -1 represents
     * invalid user orderId = engine.askLimitOrder("RandomName", new
     * Order("RandomName", ticker,5, 5, Side.ASK, Status.ACTIVE));
     * assertEquals(orderId, -1); engine.initializeUserBalance("ActualUser", 1000);
     * engine.initializeUserTickerVolume("ActualUser", ticker, 5);
     * engine.askLimitOrder("ActualUser", new Order("ActualUser", ticker, 10, 5,
     * Side.ASK, Status.ACTIVE)); engine.bidLimitOrder("ActualUser", new
     * Order("ActualUser", ticker, 8, 5, Side.BID, Status.ACTIVE)); double
     * orderFilledVolume = engine.bidMarketOrder("Buyer1", ticker, 1000);
     * assertEquals(orderFilledVolume, 0); orderFilledVolume =
     * engine.bidMarketOrder("Rasdf;", ticker, 100); assertEquals(orderFilledVolume,
     * 0); assertEquals(engine.getBidPriceLevels(ticker).size(), 1);
     * assertEquals(engine.getAskPriceLevels(ticker).size(), 1); }
     *
     * @Test void testUserListLimit() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     *
     * engine.initializeUserBalance("Trader1", 1000);
     * engine.initializeUserBalance("Trader2", 1000);
     * engine.initializeUserTickerVolume("Trader1", ticker,100);
     * engine.initializeUserTickerVolume("Trader2", ticker, 100);
     * assertEquals(engine.getUserBalance("Trader1"), 1000); Order bidOrder = new
     * Order("Trader1", ticker, 100, 5, Side.BID, Status.ACTIVE); long orderId =
     * engine.bidLimitOrder(bidOrder.name, bidOrder);
     * assertEquals(engine.getUserBalance("Trader1"), 1000); Order askOrder = new
     * Order("Trader2", ticker, 99, 5, Side.ASK, Status.ACTIVE);
     * engine.askLimitOrder("Trader2", askOrder);
     * assertEquals(engine.getTickerBalance("Trader1", ticker), 105);
     * assertEquals(engine.getUserBalance("Trader1"), 500);
     * assertEquals(engine.getUserBalance("Trader2"), 1500); // Order askOrder2 =
     * new Order("Trader1", ticker, 100, 5, Side.ASK, Status.ACTIVE);
     * engine.askLimitOrder("Trader1", askOrder2);
     * assertEquals(engine.getUserBalance("Trader1"), 500);
     * assertEquals(engine.getUserBalance("Trader2"), 1500); Order bidOrder2 = new
     * Order("Trader2", ticker, 102, 5, Side.BID, Status.ACTIVE);
     * engine.bidLimitOrder("Trader2", bidOrder2);
     * assertEquals(engine.getUserBalance("Trader1"), 1000);
     * assertEquals(engine.getUserBalance("Trader2"), 1000); }
     *
     * @Test void testCancelUserList() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("Trader1", 1000); Order bidOrder = new
     * Order("Trader1", ticker, 100, 5, Side.BID, Status.ACTIVE); long orderId =
     * engine.bidLimitOrder(bidOrder.name, bidOrder);
     * assertEquals(engine.getUserBalance("Trader1"), 1000);
     * engine.removeOrder("Trader1", orderId);
     * assertEquals(engine.getUserBalance("Trader1"), 1000); }
     *
     * @Test void testUserListMarket() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     *
     * engine.initializeUserBalance("Trader1", 1000);
     * engine.initializeUserBalance("Trader2", 500);
     * engine.initializeUserTickerVolume("Trader1", ticker, 20);
     * engine.initializeUserTickerVolume("Trader2", ticker, 20);
     *
     * Order bidOrder = new Order("Trader1", ticker, 100, 10, Side.BID,
     * Status.ACTIVE); long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
     * engine.askMarketOrder("Trader2", ticker, 5);
     * assertEquals(engine.getUserBalance("Trader1"), 500);
     * assertEquals(engine.getUserBalance("Trader2"), 1000); Order askOrder = new
     * Order("Trader1", ticker, 101, 10, Side.ASK, Status.ACTIVE); orderId =
     * engine.askLimitOrder(askOrder.name, askOrder); double volume =
     * engine.bidMarketOrder("Trader2", ticker, 10); assertEquals(1000 / 101,
     * volume); assertEquals(1000, engine.getUserBalance("Trader1"));
     * assertEquals(0, engine.getUserBalance("Trader2")); }
     *
     * @Test void testBidTrades() { MatchingEngine engine = new MatchingEngine();
     * String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("Trader1", 1000); Order bidOrder = new
     * Order("Trader1", ticker, 100, 10, Side.BID, Status.ACTIVE);
     * engine.bidLimitOrder(bidOrder.name, bidOrder); }
     *
     * @Test void testBidLimitOrderBalanceUpdates() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("Trader1", 1000);
     *
     * Order bidOrder = new Order("Trader1", ticker, 50, 10, Side.BID,
     * Status.ACTIVE); long orderId = engine.bidLimitOrder(bidOrder.name, bidOrder);
     *
     * assertEquals(1000, engine.getUserBalance("Trader1"),
     * "User balance should be reduced by bid order value"); assertEquals(0,
     * engine.getTickerBalance("Trader1", ticker),
     * "Ticker balance should remain unchanged for bid order");
     *
     * // Remove the order and verify balance restoration
     * engine.removeOrder("Trader1", orderId); assertEquals(1000,
     * engine.getUserBalance("Trader1"),
     * "User balance should be restored after bid order cancellation"); }
     *
     * @Test void testAskLimitOrderBalanceUpdates() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     * engine.initializeUserBalance("Trader1", 1000);
     * engine.initializeUserTickerVolume("Trader1", ticker, 20);
     *
     * Order askOrder = new Order("Trader1", ticker, 50, 10, Side.ASK,
     * Status.ACTIVE); long orderId = engine.askLimitOrder(askOrder.name, askOrder);
     *
     * assertEquals(1000, engine.getUserBalance("Trader1"),
     * "User balance should remain unchanged for ask order"); assertEquals(20,
     * engine.getTickerBalance("Trader1", ticker),
     * "Ticker balance should be reduced by ask order volume");
     *
     * // Remove the order and verify ticker balance restoration
     * engine.removeOrder("Trader1", orderId); assertEquals(20,
     * engine.getTickerBalance("Trader1", ticker),
     * "Ticker balance should be restored after ask order cancellation"); }
     *
     * @Test void testBidMarketOrderBalanceUpdates() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker("AAPL");
     *
     * engine.initializeUserBalance("Buyer1", 500);
     * engine.initializeUserBalance("Seller1", 500);
     * engine.initializeUserTickerVolume("Seller1", ticker, 10);
     * engine.initializeUserTickerVolume("Buyer1", ticker, 0); // Seller posts an
     * ask order engine.askLimitOrder("Seller1", new Order("Seller1", ticker, 50,
     * 10, Side.ASK, Status.ACTIVE));
     *
     * // Buyer places a market order double volumeFilled =
     * engine.bidMarketOrder("Buyer1", ticker, 10);
     *
     * assertEquals(10, volumeFilled,
     * "Market order should fully match the ask order volume"); assertEquals(0,
     * engine.getUserBalance("Buyer1"), "Buyer's balance should be fully consumed");
     * assertEquals(10, engine.getTickerBalance("Buyer1", ticker),
     * "Buyer's ticker balance should increase by matched volume");
     * assertEquals(1000, engine.getUserBalance("Seller1"),
     * "Seller's balance should increase by the transaction value"); assertEquals(0,
     * engine.getTickerBalance("Seller1", ticker),
     * "Seller's ticker balance should be reduced by the sold volume"); }
     *
     * @Test void testRemoveAllOrdersRestoresBalances() { MatchingEngine engine =
     * new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker("AAPL"); engine.initializeUserBalance("Trader1",
     * 1000); engine.initializeUserTickerVolume("Trader1", ticker, 20);
     *
     * // Place multiple orders engine.bidLimitOrder("Trader1", new Order("Trader1",
     * ticker, 50, 10, Side.BID, Status.ACTIVE)); engine.askLimitOrder("Trader1",
     * new Order("Trader1", ticker, 60, 5, Side.ASK, Status.ACTIVE));
     *
     * // Remove all orders and verify balances engine.removeAll("Trader1");
     * assertEquals(1000, engine.getUserBalance("Trader1"),
     * "User balance should be restored after removing all orders");
     * assertEquals(20, engine.getTickerBalance("Trader1", ticker),
     * "Ticker balance should be restored after removing all orders"); }
     *
     * @Test void testRemoveOrderRestoresAllProperties() { MatchingEngine engine =
     * new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker(ticker); engine.initializeUserBalance("Trader1",
     * 1000); engine.initializeUserTickerVolume("Trader1", ticker, 20);
     *
     * // Place an order Order bidOrder = new Order("Trader1", ticker, 100, 1,
     * Side.BID, Status.ACTIVE); long orderId = engine.bidLimitOrder(bidOrder.name,
     * bidOrder);
     *
     * // Verify initial state assertEquals(1000, engine.getUserBalance("Trader1"),
     * "User balance should reflect bid reservation"); assertEquals(20,
     * engine.getTickerBalance("Trader1", ticker),
     * "Ticker volume should remain unchanged for bid orders");
     *
     * // Remove the order boolean removed = engine.removeOrder("Trader1", orderId);
     *
     * // Verify removal assertTrue(removed,
     * "Order should be successfully removed"); assertEquals(1000,
     * engine.getUserBalance("Trader1"),
     * "User balance should be restored after removing bid order"); assertEquals(20,
     * engine.getTickerBalance("Trader1", ticker),
     * "Ticker balance should remain unchanged after removing bid order");
     * assertNull(engine.getOrder("Trader1", orderId),
     * "Getting Order Should be Null"); }
     *
     * @Test void testRemoveAllRestoresAllProperties() { MatchingEngine engine = new
     * MatchingEngine(); String ticker = "AAPL"; engine.initializeTicker(ticker);
     * engine.initializeUserBalance("Trader1", 1000);
     * engine.initializeUserTickerVolume("Trader1", ticker, 20);
     *
     * // Place multiple orders engine.bidLimitOrder("Trader1", new Order("Trader1",
     * ticker, 50, 10, Side.BID, Status.ACTIVE)); engine.askLimitOrder("Trader1",
     * new Order("Trader1", ticker, 60, 5, Side.ASK, Status.ACTIVE));
     *
     * // Verify initial state assertEquals(1000, engine.getUserBalance("Trader1"),
     * "User balance should reflect bid reservation"); assertEquals(15,
     * engine.getTickerBalance("Trader1", ticker),
     * "Ticker volume should reflect ask reservation");
     *
     * // Remove all orders engine.removeAll("Trader1");
     *
     * // Verify all properties are restored assertEquals(1000,
     * engine.getUserBalance("Trader1"),
     * "User balance should be fully restored after removing all orders");
     * assertEquals(20, engine.getTickerBalance("Trader1", ticker),
     * "Ticker balance should be fully restored after removing all orders");
     * List<PriceLevel> bidLevels = engine.getBidPriceLevels(ticker);
     * List<PriceLevel> askLevels = engine.getAskPriceLevels(ticker);
     * assertEquals(0, bidLevels.size(), "All bid orders should be removed");
     * assertEquals(0, askLevels.size(), "All ask orders should be removed"); }
     *
     * @Test void testRemoveOrderForAskRestoresAllProperties() { MatchingEngine
     * engine = new MatchingEngine(); String ticker = "AAPL";
     * engine.initializeTicker(ticker); engine.initializeUserBalance("Trader1",
     * 1000); engine.initializeUserTickerVolume("Trader1", ticker, 20);
     *
     * // Place an ask order Order askOrder = new Order("Trader1", ticker, 100, 10,
     * Side.ASK, Status.ACTIVE); long orderId = engine.askLimitOrder(askOrder.name,
     * askOrder);
     *
     * // Verify initial state assertEquals(1000, engine.getUserBalance("Trader1"),
     * "User balance should remain unchanged for ask orders"); assertEquals(20,
     * engine.getTickerBalance("Trader1", ticker),
     * "Ticker balance should reflect ask reservation");
     *
     * // Remove the ask order boolean removed = engine.removeOrder("Trader1",
     * orderId);
     *
     * // Verify removal assertTrue(removed,
     * "Ask order should be successfully removed"); assertEquals(1000,
     * engine.getUserBalance("Trader1"),
     * "User balance should remain unchanged after removing ask order");
     * assertEquals(20, engine.getTickerBalance("Trader1", ticker),
     * "Ticker balance should be fully restored after removing ask order");
     * assertNull(engine.getOrder("Trader1", orderId),
     * "Order status should be CANCELLED"); List<PriceLevel> askLevels =
     * engine.getAskPriceLevels(ticker); assertEquals(0, askLevels.size(),
     * "Ask price levels should be empty after removing the order"); }
     *
     * @Test void initializeAllTickers() { MatchingEngine matchingEngine = new
     * MatchingEngine(); matchingEngine.initializeAllTickers(); }
     *
     * @Test void initializeAllUsers() { MatchingEngine matchingEngine = new
     * MatchingEngine(); matchingEngine.initializeUser("Trader1"); }
     *
     * @Test public void testAddTradeAndCheckVolumeSummation() { MatchingEngine
     * engine = new MatchingEngine(); RecentTrades.getRecentTrades(); String ticker
     * = "AAPL"; Map<Double, Double> volumeMap = new TreeMap<>(); Side side =
     * Side.BID;
     *
     * engine.updateVolume(volumeMap, 100, 10, ticker, side);
     * engine.updateVolume(volumeMap, 100, 5, ticker, side);
     *
     * ArrayList<PriceChange> trades = RecentTrades.getRecentTrades();
     * assertFalse(trades.isEmpty(), "Trades list should not be empty");
     * assertEquals(1, trades.size(), "There should be exactly one trade recorded");
     *
     * PriceChange trade = trades.get(0); assertEquals(100, trade.getPrice(),
     * "Trade price should match the updated price"); assertEquals(15,
     * trade.getVolume(), "Trade volume should be the sum of updates");
     * assertEquals(side, trade.getSide(),
     * "Trade side should match the updated side"); }
     *
     * @Test public void testTradeLoggingForDifferentPrices() { MatchingEngine
     * engine = new MatchingEngine(); RecentTrades.getRecentTrades(); String ticker
     * = "AAPL"; Map<Double, Double> volumeMap = new TreeMap<>(); Side side =
     * Side.ASK;
     *
     * engine.updateVolume(volumeMap, 100, 10, ticker, side);
     * engine.updateVolume(volumeMap, 105, 20, ticker, side);
     *
     * ArrayList<PriceChange> trades = RecentTrades.getRecentTrades();
     * assertEquals(2, trades.size(), "Should log trades at two different prices");
     *
     * // Checking each trade PriceChange firstTrade = trades.get(0); PriceChange
     * secondTrade = trades.get(1);
     *
     * if (firstTrade.getPrice() == 100) { assertEquals(10, firstTrade.getVolume(),
     * "Volume at price 100 should be 10"); assertEquals(105,
     * secondTrade.getPrice(), "Second trade price should be 105"); assertEquals(20,
     * secondTrade.getVolume(), "Volume at price 105 should be 20"); } else {
     * assertEquals(20, firstTrade.getVolume(), "Volume at price 105 should be 20");
     * assertEquals(100, secondTrade.getPrice(),
     * "Second trade price should be 100"); assertEquals(10,
     * secondTrade.getVolume(), "Volume at price 100 should be 10"); } }
     *
     * @Test public void testTradeClearingPostRetrieval() { MatchingEngine engine =
     * new MatchingEngine(); RecentTrades.getRecentTrades(); String ticker = "AAPL";
     * Map<Double, Double> volumeMap = new TreeMap<>(); Side side = Side.BID;
     *
     * engine.updateVolume(volumeMap, 100, 10, ticker, side);
     *
     * // Get and clear trades assertNotNull(RecentTrades.getRecentTrades(),
     * "Should return a non-null list of trades");
     * assertTrue(RecentTrades.getRecentTrades().isEmpty(),
     * "Trades should be cleared after retrieval"); }
     *
     * @Test public void testLimitOrderTradeLogging() { MatchingEngine engine = new
     * MatchingEngine(); RecentTrades.getRecentTrades(); String ticker = "GOOG";
     * engine.initializeTicker(ticker); engine.initializeUserBalance("TraderC",
     * 2000); engine.initializeUserTickerVolume("TraderC", ticker, 50);
     *
     * // Place a bid limit order and an ask limit order that should match
     * engine.bidLimitOrder("TraderC", new Order("TraderC", ticker, 150, 5,
     * Side.BID, Status.ACTIVE)); engine.askLimitOrder("TraderC", new
     * Order("TraderC", ticker, 150, 5, Side.ASK, Status.ACTIVE));
     *
     * // Check trades logged List<PriceChange> trades =
     * RecentTrades.getRecentTrades(); assertFalse(trades.isEmpty(),
     * "Trades should be logged for matching orders"); assertEquals(1,
     * trades.size(), "One trade should be logged for the matched orders");
     *
     * PriceChange trade = trades.get(0); assertEquals(150, trade.getPrice(),
     * "Trade price should match the order price"); assertEquals(0,
     * trade.getVolume(), "Trade volume should match the order volume");
     * assertNotNull(trade.getSide(), "Trade should have a side"); }
     *
     * @Test public void testMarketOrderTradeLogging() { MatchingEngine engine = new
     * MatchingEngine(); RecentTrades.getRecentTrades(); String ticker = "GOOG";
     * engine.initializeTicker(ticker); engine.initializeUserBalance("TraderD",
     * 5000); engine.initializeUserTickerVolume("TraderD", ticker, 100);
     *
     * // Set up limit orders to be matched by a market order
     * engine.askLimitOrder("TraderD", new Order("TraderD", ticker, 200, 20,
     * Side.ASK, Status.ACTIVE)); engine.askLimitOrder("TraderD", new
     * Order("TraderD", ticker, 205, 20, Side.ASK, Status.ACTIVE));
     * List<PriceChange> trades = RecentTrades.getRecentTrades(); assertEquals(2,
     * trades.size()); // Place market buy order that matches the above asks
     * engine.bidMarketOrder("TraderD", ticker, 30);
     *
     * // Check trades logged trades = RecentTrades.getRecentTrades();
     * assertEquals(2, trades.size(),
     * "Two trades should be logged for the matched market order");
     *
     * // Check first trade details PriceChange firstTrade = trades.get(0);
     * assertEquals(200, firstTrade.getPrice(), "First trade price should be 200");
     * assertEquals(0, firstTrade.getVolume(), "Volume at level should now be 0");
     *
     * // Check second trade details PriceChange secondTrade = trades.get(1);
     * assertEquals(205, secondTrade.getPrice(),
     * "Second trade price should be 205"); assertEquals(10,
     * secondTrade.getVolume(), "Volume at level show now be 10"); }
     *
     * @Test public void testCancelOrderTradeLogging() { MatchingEngine engine = new
     * MatchingEngine(); RecentTrades.getRecentTrades(); String ticker = "GOOG";
     * engine.initializeTicker(ticker); engine.initializeUserBalance("TraderE",
     * 10000); engine.initializeUserTickerVolume("TraderE", ticker, 50);
     *
     * // Place and cancel a bid limit order long orderId =
     * engine.bidLimitOrder("TraderE", new Order("TraderE", ticker, 250, 10,
     * Side.BID, Status.ACTIVE)); engine.removeOrder("TraderE", orderId);
     *
     * // Check that no trades were logged for the cancellation List<PriceChange>
     * trades = RecentTrades.getRecentTrades(); System.out.println(trades);
     * assertEquals(trades.size(),1, "Cancel is also in the map"); } private static
     * final double POSITION_LIMIT = 100;
     *
     * /** /** REWRITTEN INFINITE TESTS - ALL ABOVE ARE BEING REFACTORED
     **/
    @Test
    void infiniteTest1() {
        int positionLimit = 100;
        MatchingEngine engine = new MatchingEngine(positionLimit);
        String ticker = "AAPL";
        engine.initializeTicker("AAPL");
        // Trader with infinite balance
        String buyer = "InfiniteTrader";
        // Seller with regular balance
        String seller1 = "Seller1";
        String seller2 = "Seller2";
        engine.initializeUserBalance(seller1, 0);
        engine.initializeUserBalance(seller2, 0);
        engine.initializeUserBalance(buyer, 0);
        engine.initializeUserTickerVolume(seller1, "AAPL", 0);
        engine.initializeUserTickerVolume(seller2, "AAPL", 0);
        engine.initializeUserTickerVolume(buyer, "AAPL", 0);
        // Two sellers place sell orders
        engine.askLimitOrder(seller1, new Order(seller1, "AAPL", 150, 60, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(seller2, new Order(seller2, "AAPL", 150, 40, Side.ASK, Status.ACTIVE));
        long orderId = engine.askLimitOrder(seller1, new Order(seller1, "AAPL", 150, 40, Side.ASK, Status.ACTIVE));
        assertEquals(
                engine.getUserBalance(seller1), 0); // Shouldn't get more balance before placing the trade
        assertEquals(0, engine.getTickerBalance(seller1, "AAPL"));
        // Buyer places two market orders
        double firstFill = engine.bidMarketOrder(buyer, "AAPL", 60);
        double secondFill = engine.bidMarketOrder(buyer, "AAPL", 60);
        System.out.println(firstFill);
        System.out.println(secondFill);
        // Ensure only up to the position limit was filled
        assertEquals(
                100,
                firstFill + secondFill,
                "Trader should not be able to own more than the position limit");

        // Ensure the trader's position does not exceed the limit
        assertEquals(
                100,
                engine.getTickerBalance(buyer, "AAPL"),
                "Trader's position should not exceed the position limit");
        assertEquals(-15000, engine.getUserBalance(buyer));
        assertEquals(9000, engine.getUserBalance(seller1));
        assertEquals(6000, engine.getUserBalance(seller2));
        assertEquals(
                0,
                engine.getUserBalance(buyer)
                        + engine.getUserBalance(seller1)
                        + engine.getUserBalance(seller2));
        assertEquals(
                0,
                engine.getTickerBalance(buyer, ticker)
                        + engine.getTickerBalance(seller1, ticker)
                        + engine.getTickerBalance(seller2, ticker));
        engine.removeOrder(seller1, orderId);
        assertEquals(
                0,
                engine.getTickerBalance(buyer, ticker)
                        + engine.getTickerBalance(seller1, ticker)
                        + engine.getTickerBalance(seller2, ticker));
    }

    @Test
    public void infiniteTestTradingWithSelf() {
        int positionLimit = 1000;
        MatchingEngine engine = new MatchingEngine(positionLimit);
        String user = "TraderZ";
        String ticker = "A";
        engine.initializeUserBalance(user, 0);
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user, ticker, 0); // Long 50 shares

        // Try to buy up to position limit
        System.out.println("This Far");
        Order buyOrder = new Order(user, ticker, 150, 50, Side.BID, Status.ACTIVE); // Buy additional 50 to reach 100
        long orderId = engine.bidLimitOrder(user, buyOrder);
        System.out.println(orderId);
        assertTrue(orderId > 0, "Should allow buying within position limit");
        assertEquals(0, engine.getTickerBalance(user, ticker));
        assertEquals(0, engine.getUserBalance(user));
        // Try to short up to position limit
        Order sellOrder = new Order(
                user, ticker, 150, 50, Side.ASK, Status.ACTIVE); // Short additional 50 to reach -100
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
        MatchingEngine engine = new MatchingEngine(positionLimit);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
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
        MatchingEngine engine = new MatchingEngine(positionLimit);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
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
        MatchingEngine engine = new MatchingEngine(positionLimit);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
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
        MatchingEngine engine = new MatchingEngine(positionLimit);
        String user1 = "u1";
        String user2 = "u2";
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);

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
        MatchingEngine engine = new MatchingEngine(positionLimit);
        String user1 = "u1";
        String user2 = "u2";
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);

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
        MatchingEngine engine = new MatchingEngine(positionLimit);
        String user1 = "u1";
        String user2 = "u2";
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);

        Map<String, Object> message = engine.askLimitOrderHandler(
                user1, new Order(user1, ticker, 15, 80, Side.ASK, Status.ACTIVE));
        assertEquals(0, (double) message.get("price"));
        assertEquals(0, (int) message.get("volumeFilled"));
        message = engine.bidLimitOrderHandler(
                user2, new Order(user2, ticker, 16, 10, Side.BID, Status.ACTIVE));
        assertEquals(15, (double) message.get("price"));
        assertEquals(10, (int) message.get("volumeFilled"));
        message = engine.askLimitOrderHandler(
                user1, new Order(user1, ticker, 14, 5, Side.ASK, Status.ACTIVE));
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
        MatchingEngine engine = new MatchingEngine(positionLimit);
        String user1 = "u1";
        String user2 = "u2";
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);

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
        MatchingEngine engine = new MatchingEngine(positionLimit);
        String user1 = "u1";
        String user2 = "u2";
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);

        engine.askLimitOrder(user1, new Order(user1, ticker, 25, 8, Side.ASK, Status.ACTIVE));
        engine.askLimitOrder(user1, new Order(user1, ticker, 28, 2, Side.ASK, Status.ACTIVE));
        Map<String, Object> message = engine.bidLimitOrderHandler(
                user2, new Order(user2, ticker, 30, 12, Side.BID, Status.ACTIVE));
        assertEquals((25.0 * 8 + 28.0 * 2) / 10.0, (double) message.get("price"));
        assertEquals(10, message.get("volumeFilled"));
    }

    @Test
    public void testAskLimitOrderPrice() {
        String ticker = "D";
        int positionLimit = 100;
        MatchingEngine engine = new MatchingEngine(positionLimit);
        String user1 = "u1";
        String user2 = "u2";
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);

        engine.bidLimitOrder(user2, new Order(user2, ticker, 30, 10, Side.BID, Status.ACTIVE));
        engine.bidLimitOrder(user2, new Order(user2, ticker, 29, 3, Side.BID, Status.ACTIVE));
        Map<String, Object> message = engine.askLimitOrderHandler(
                user1, new Order(user1, ticker, 27, 11, Side.ASK, Status.ACTIVE));
        double price = (30.0 * 10 + 29 * 1) / 11;
        assertEquals(price, (double) message.get("price"));
        assertEquals(11, message.get("volumeFilled"));
        assertEquals(-(30 * 10 + 29 * 1), engine.getUserBalance(user2));
        assertEquals((30 * 10 + 29 * 1), engine.getUserBalance(user1));
    }

    // Place Bid LimitOrder and its filled
    // askLimitOrder
    @Test
    public void infiniteBrokenTest() {
        String ticker = "D";
        int positionLimit = 100;
        MatchingEngine engine = new MatchingEngine(positionLimit);
        String user1 = "u1";
        String user2 = "u2";
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);

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
        MatchingEngine engine = new MatchingEngine(positionLimit);
        String user1 = "u1";
        String user2 = "u2";
        String user3 = "u3";
        engine.initializeTicker(ticker);
        engine.initializeUserTickerVolume(user1, ticker, 0);
        engine.initializeUserTickerVolume(user2, ticker, 0);
        engine.initializeUserTickerVolume(user3, ticker, 0);
        engine.initializeUserBalance(user1, 0);
        engine.initializeUserBalance(user2, 0);
        engine.initializeUserBalance(user3, 0);
        long orderId = engine.bidLimitOrder(user1, new Order(user1, ticker, 200, 1000, Side.BID, Status.ACTIVE));
        assertEquals(1, orderId);
        orderId = engine.askLimitOrder(user1, new Order(user1, ticker, 250, 1000, Side.BID, Status.ACTIVE));
        assertEquals(2, orderId);

        // User 2 will buy a market order and then user 3 will provide multiple levels
        // to sell it at
        double volumeFilled = engine.bidMarketOrder(user2, ticker, 1000);
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
}
