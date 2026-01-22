package hte.matchingengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hte.common.Message;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests for order placement rules. */
public class OrderPlacementRulesTest {

    private static final String TICKER = "A";

    private static int errorCode(Map<String, Object> resp) {
        return (int) resp.get("errorCode");
    }

    @Test
    void finite_user_limitBid_requiresSufficientCash() {
        MatchingEngine engine = TestEngines.finiteSingleTicker(TICKER);
        TestEngines.initUser(engine, "u", /*cash*/ 100, TICKER, /*qty*/ 0);

        // price*volume = 101 > cash
        long orderId =
                engine.bidLimitOrder("u", new Order("u", TICKER, 101, 1, Side.BID, Status.ACTIVE));
        assertEquals(-1, orderId);

        orderId = engine.bidLimitOrder("u", new Order("u", TICKER, 50, 2, Side.BID, Status.ACTIVE));
        assertTrue(orderId > 0);
    }

    @Test
    void finite_user_limitAsk_requiresSufficientInventory_andAccountsForReservations() {
        MatchingEngine engine = TestEngines.finiteSingleTicker(TICKER);
        TestEngines.initUser(engine, "u", /*cash*/ 0, TICKER, /*qty*/ 10);

        long first =
                engine.askLimitOrder("u", new Order("u", TICKER, 10, 10, Side.ASK, Status.ACTIVE));
        assertTrue(first > 0);

        // already reserved 10 => cannot place any additional asks in finite mode
        long second =
                engine.askLimitOrder("u", new Order("u", TICKER, 11, 1, Side.ASK, Status.ACTIVE));
        assertEquals(-1, second);
    }

    @Test
    void finite_user_marketBid_requiresLiquidity_andEnoughCashAtBestAsk() {
        MatchingEngine engine = TestEngines.finiteSingleTicker(TICKER);
        TestEngines.initUser(engine, "buyerPoor", /*cash*/ 0, TICKER, /*qty*/ 0);
        TestEngines.initUser(engine, "buyerRich", /*cash*/ 200, TICKER, /*qty*/ 0);
        TestEngines.initUser(engine, "seller", /*cash*/ 0, TICKER, /*qty*/ 10);

        // Seed book: seller posts ask 1 @ 100.
        long askId =
                engine.askLimitOrder(
                        "seller", new Order("seller", TICKER, 100, 1, Side.ASK, Status.ACTIVE));
        assertTrue(askId > 0);

        Map<String, Object> resp = engine.bidMarketOrderHandler("buyerPoor", TICKER, 1);
        assertEquals(Message.INSUFFICIENT_BALANCE.getErrorCode(), errorCode(resp));

        // A different user with enough cash should be able to fill.
        resp = engine.bidMarketOrderHandler("buyerRich", TICKER, 1);
        assertEquals(0, errorCode(resp));
        assertEquals(1, (int) resp.get("volumeFilled"));
    }

    @Test
    void finite_user_marketAsk_requiresLiquidity_andAvailableInventory_minusReservations() {
        MatchingEngine engine = TestEngines.finiteSingleTicker(TICKER);
        TestEngines.initUser(engine, "seller", /*cash*/ 0, TICKER, /*qty*/ 1);
        TestEngines.initUser(engine, "buyer", /*cash*/ 1000, TICKER, /*qty*/ 0);

        // Provide bid liquidity.
        long bidId =
                engine.bidLimitOrder(
                        "buyer", new Order("buyer", TICKER, 100, 10, Side.BID, Status.ACTIVE));
        assertTrue(bidId > 0);

        // Reserve the single share via a resting ask.
        long askId =
                engine.askLimitOrder(
                        "seller", new Order("seller", TICKER, 200, 1, Side.ASK, Status.ACTIVE));
        assertTrue(askId > 0);

        // Now market-sell should be rejected: available inventory is 0 due to reservation.
        Map<String, Object> resp = engine.askMarketOrderHandler("seller", TICKER, 1);
        assertEquals(Message.INSUFFICIENT_TICKER_BALANCE.getErrorCode(), errorCode(resp));
    }

    @Test
    void infinite_user_limitBid_enforcesPositionLimit() {
        int positionLimit = 5;
        MatchingEngine engine = TestEngines.infiniteSingleTicker(positionLimit, TICKER);
        TestEngines.initUser(engine, "u", /*cash*/ 0, TICKER, /*qty*/ 0);

        // Buy up to limit is allowed
        assertTrue(
                engine.bidLimitOrder("u", new Order("u", TICKER, 10, 5, Side.BID, Status.ACTIVE))
                        > 0);

        // Further bid exceeds remaining capacity (reserved bids count too)
        assertEquals(
                -1,
                engine.bidLimitOrder("u", new Order("u", TICKER, 11, 1, Side.BID, Status.ACTIVE)));
    }

    @Test
    void infinite_user_limitAsk_enforcesPositionLimit_allowsShortWithinLimit() {
        int positionLimit = 5;
        MatchingEngine engine = TestEngines.infiniteSingleTicker(positionLimit, TICKER);
        TestEngines.initUser(engine, "u", /*cash*/ 0, TICKER, /*qty*/ 0);

        // Can short-sell up to limit
        assertTrue(
                engine.askLimitOrder("u", new Order("u", TICKER, 10, 5, Side.ASK, Status.ACTIVE))
                        > 0);

        // Additional ask exceeds -positionLimit once reservations are counted
        assertEquals(
                -1,
                engine.askLimitOrder("u", new Order("u", TICKER, 11, 1, Side.ASK, Status.ACTIVE)));
    }

    @Test
    void bot_limitOrders_requireBasicSanity_andSkipBalanceInventoryConstraints_finite() {
        MatchingEngine engine = TestEngines.finiteSingleTicker(TICKER);
        TestEngines.initUser(engine, "maker", /*cash*/ 0, TICKER, /*qty*/ 10);

        engine.initializeBot("bot");
        engine.initializeUserTickerVolume("bot", TICKER, 0);

        // Basic sanity: price and volume must be positive.
        assertEquals(
                -1,
                engine.bidLimitOrder(
                        "bot", new Order("bot", TICKER, 0, 1, Side.BID, Status.ACTIVE)));
        assertEquals(
                -1,
                engine.askLimitOrder(
                        "bot", new Order("bot", TICKER, 1, 0, Side.ASK, Status.ACTIVE)));

        // Can place asks without inventory, and bids without cash.
        assertTrue(
                engine.askLimitOrder(
                                "bot", new Order("bot", TICKER, 100, 10, Side.ASK, Status.ACTIVE))
                        > 0);
        assertTrue(
                engine.bidLimitOrder(
                                "bot", new Order("bot", TICKER, 99, 10, Side.BID, Status.ACTIVE))
                        > 0);

        // Unknown ticker is rejected (sanity check).
        assertEquals(
                -1,
                engine.bidLimitOrder(
                        "bot", new Order("bot", "UNKNOWN", 99, 1, Side.BID, Status.ACTIVE)));
    }

    @Test
    void bot_marketOrders_skipBalanceInventoryConstraints_infinite() {
        int positionLimit = 10;
        MatchingEngine engine = TestEngines.infiniteSingleTicker(positionLimit, TICKER);

        TestEngines.initUser(engine, "seller", /*cash*/ 0, TICKER, /*qty*/ 10);
        TestEngines.initUser(engine, "buyer", /*cash*/ 1000, TICKER, /*qty*/ 0);
        engine.initializeBot("bot");
        engine.initializeUserTickerVolume("bot", TICKER, 0);

        // Seed liquidity on both sides.
        assertTrue(
                engine.askLimitOrder(
                                "seller",
                                new Order("seller", TICKER, 100, 5, Side.ASK, Status.ACTIVE))
                        > 0);
        assertTrue(
                engine.bidLimitOrder(
                                "buyer", new Order("buyer", TICKER, 90, 5, Side.BID, Status.ACTIVE))
                        > 0);

        // Bot buys without cash.
        Map<String, Object> buy = engine.bidMarketOrderHandler("bot", TICKER, 5);
        assertEquals(0, errorCode(buy));
        assertEquals(5, (int) buy.get("volumeFilled"));

        // Bot sells without inventory.
        Map<String, Object> sell = engine.askMarketOrderHandler("bot", TICKER, 5);
        assertEquals(0, errorCode(sell));
        assertEquals(5, (int) sell.get("volumeFilled"));
    }
}
