package hte.matchingengine;

/** Test-only helpers for building deterministic {@link MatchingEngine} setups. */
public final class TestEngines {
    private TestEngines() {}

    public static MatchingEngine finiteSingleTicker(String ticker) {
        MatchingEngine engine = new MatchingEngine();
        engine.initializeTicker(ticker);
        return engine;
    }

    public static MatchingEngine infiniteSingleTicker(int positionLimit, String ticker) {
        MatchingEngine engine = new MatchingEngine(positionLimit);
        engine.initializeTicker(ticker);
        return engine;
    }

    public static void initUser(
            MatchingEngine engine, String username, int cashBalance, String ticker, int quantity) {
        engine.initializeUserBalance(username, cashBalance);
        engine.initializeUserTickerVolume(username, ticker, quantity);
    }

    public static void initBot(
            MatchingEngine engine, String username, String ticker, int quantity) {
        engine.initializeBot(username);
        engine.initializeUserTickerVolume(username, ticker, quantity);
    }

    public static long seedAsk(
            MatchingEngine engine, String username, String ticker, int price, int volume) {
        return engine.askLimitOrder(
                username, new Order(username, ticker, price, volume, Side.ASK, Status.ACTIVE));
    }

    public static long seedBid(
            MatchingEngine engine, String username, String ticker, int price, int volume) {
        return engine.bidLimitOrder(
                username, new Order(username, ticker, price, volume, Side.BID, Status.ACTIVE));
    }
}
