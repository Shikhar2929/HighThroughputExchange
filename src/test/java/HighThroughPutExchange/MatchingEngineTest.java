package HighThroughPutExchange;
import static org.junit.jupiter.api.Assertions.*;

import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import HighThroughPutExchange.MatchingEngine.Order;
import HighThroughPutExchange.MatchingEngine.Side;
import HighThroughPutExchange.MatchingEngine.Status;
import java.util.*;
import org.junit.jupiter.api.Test;

public class MatchingEngineTest {
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
        Map<String, Object> message = engine.bidLimitOrderHandler(user2,
                new Order(user2, ticker, 30, 12, Side.BID, Status.ACTIVE));
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
        Map<String, Object> message = engine.askLimitOrderHandler(user1,
                new Order(user1, ticker, 27, 11, Side.ASK, Status.ACTIVE));
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
