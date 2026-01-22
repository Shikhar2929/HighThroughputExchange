package hte.matchingengine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hte.common.ChartTrackerSingleton;
import hte.common.Message;
import hte.common.TaskFuture;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core order matching engine.
 *
 * <p>Maintain order books per ticker (bids/asks) and last traded/marked prices. Validate and place
 * limit orders and execute market orders against the book. Maintain per-user state via {@link
 * UserList} (cash balance, per-ticker inventory, reserved bid/ask quantities). Expose helper
 * methods used by controllers/services to query state.
 *
 * <p>API response conventions:
 *
 * <p>Limit orders return a map with {@code price}, {@code volumeFilled}, {@code orderId}, {@code
 * errorCode}, {@code errorMessage}. Market orders return a map with {@code price}, {@code
 * volumeFilled}, {@code errorCode}, {@code errorMessage}. For market orders, {@code price} is the
 * volume-weighted average execution price. {@code errorCode == 0} indicates success; partial fills
 * are treated as success.
 */
public class MatchingEngine {
    private static final Logger logger = LoggerFactory.getLogger(MatchingEngine.class);

    private static final String DEFAULT_CONFIG_LOCATION = "assets/config.infinite.json";

    /**
     * Config location for engine initialization JSON (filesystem path or {@code classpath:...}).
     */
    private final String configLocation;

    // Ticker -> per-ticker order book (bids/asks + aggregated volumes).
    private Map<String, OrderBook> orderBooks = new HashMap<>();

    // Username -> (orderId -> Order). Tracks a user's active orders for cancellation/lookup.
    private Map<String, Map<Long, Order>> userOrders = new HashMap<>(); // UserName, OrderId, Order

    // Ticker -> last known traded/mark price (used for PnL/leaderboard and UI).
    private Map<String, Integer> latestPrice = new HashMap<>(); // For PnL

    // Bot username -> placeholder (presence indicates bot).
    private Map<String, Integer> bots = new HashMap<>();

    // Singleton chart tracker used to publish price updates.
    private ChartTrackerSingleton chartTrackerSingleton = ChartTrackerSingleton.getInstance();

    // In-memory user state (cash, positions, reservations).
    private UserList userList = new UserList();

    // Monotonically increasing order id counter for new resting limit orders.
    private long orderID = 0;

    public MatchingEngine() {
        this(false, DEFAULT_CONFIG_LOCATION);
    }

    /** Creates a matching engine and optionally initializes game mode from the configured JSON. */
    public MatchingEngine(boolean initialize) {
        this(initialize, DEFAULT_CONFIG_LOCATION);
    }

    /**
     * Creates a matching engine and optionally initializes game mode from {@code configLocation}.
     *
     * <p>{@code configLocation} supports filesystem paths (e.g. {@code config.json}) and classpath
     * resources via {@code classpath:config.json}.
     */
    public MatchingEngine(boolean initialize, String configLocation) {
        this.configLocation =
                (configLocation == null || configLocation.isBlank())
                        ? DEFAULT_CONFIG_LOCATION
                        : configLocation;

        // Default engine runs in finite mode unless configured otherwise.
        userList.setInfinite(false);

        if (initialize) {
            initializeGameMode();
        }
    }

    /**
     * Creates an "infinite mode" matching engine with a position limit.
     *
     * <p>In infinite mode, users are initialized without a fixed cash balance; trading capacity is
     * limited by {@code positionLimit}.
     */
    public MatchingEngine(int positionLimit) {
        this(false, DEFAULT_CONFIG_LOCATION);
        userList.setInfinite(true);
        userList.setPositionLimit(positionLimit);
    }

    /**
     * Serializes all order books to JSON.
     *
     * @return JSON representation of {@code orderBooks}, or {@code null} if serialization fails.
     */
    public String serializeOrderBooks() {
        // Note: prints internal book state to logs before serializing.
        ObjectMapper mapper = new ObjectMapper();
        for (String key : orderBooks.keySet()) {
            orderBooks.get(key).printOrderBook();
        }
        try {
            return mapper.writeValueAsString(orderBooks);
        } catch (Exception e) {
            logger.error("Failed to serialize order books", e);
            return null;
        }
    }

    private void initializeGameMode() {
        // Reads config JSON and sets finite vs infinite mode (+ limits in infinite mode).
        try {
            JsonNode configData = readConfigJson();
            String mode = configData.path("mode").asText("");
            if (mode.equals("finite")) {
                userList.setInfinite(false);
            } else {
                userList.setInfinite(true);
                JsonNode defaults = configData.path("defaults");
                int positionLimit = defaults.path("positionLimit").asInt();
                userList.setPositionLimit(positionLimit);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize game mode from config: {}", configLocation, e);
        }
    }

    private JsonNode readConfigJson() throws IOException {
        try (BufferedReader reader =
                Files.newBufferedReader(Path.of(configLocation), StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                content.append(buf, 0, n);
            }
            return new ObjectMapper().readTree(content.toString());
        }
    }

    /**
     * Initializes a bot user with zero balance and zero inventory for all currently known tickers.
     *
     * <p>Bots are treated as always-valid users for most validations.
     */
    public boolean initializeBot(String username) {
        bots.put(username, 0);
        // Bots are always allowed to have negative cash balances (infinite money to lose),
        // even when the engine is running in finite mode.
        userList.allowNegativeBalance(username);
        initializeUserBalance(username, 0);
        for (String ticker : orderBooks.keySet()) {
            initializeUserTickerVolume(username, ticker, 0);
        }
        return true;
    }

    /**
     * Initializes a user's cash balance.
     *
     * <p>Note: in finite mode this is a one-time initialization; subsequent calls for the same
     * username return {@code false} and do not modify the existing balance.
     */
    public boolean initializeUserBalance(String username, int balance) {
        logger.info("Initializing user balance: username={} balance={}", username, balance);
        return userList.initializeUser(username, balance);
    }

    /**
     * Initializes/overwrites a user's inventory for a given ticker.
     *
     * @return true if initialization succeeded.
     */
    public boolean initializeUserTickerVolume(String username, String ticker, int volume) {
        logger.info(
                "Initializing user ticker volume: username={} ticker={} volume={}",
                username,
                ticker,
                volume);
        return userList.initializeUserQuantity(username, ticker, volume);
    }

    /**
     * @return the user's cash balance (integer).
     */
    public int getUserBalance(String username) {
        return (int) userList.getUserBalance(username);
    }

    /**
     * Unrealized PnL computed using {@code latestPrice} per ticker.
     *
     * @return unrealized PnL.
     */
    public long getPnl(String username) {
        return userList.getUnrealizedPnl(username, latestPrice);
    }

    public ArrayList<PriceChange> getRecentTrades() {
        ArrayList<PriceChange> recentTrades = RecentTrades.getRecentTrades();
        // for (Trade trade : recentTrades)
        /// System.out.println(trade);
        return recentTrades;
    }

    /**
     * @return the user's inventory for {@code ticker}.
     */
    public int getTickerBalance(String username, String ticker) {
        return userList.getUserVolume(username, ticker);
    }

    /**
     * Initializes a single ticker order book.
     *
     * @return false if the ticker already exists.
     */
    public boolean initializeTicker(String ticker) {
        if (orderBooks.containsKey(ticker)) {
            return false;
        }
        orderBooks.put(ticker, new OrderBook());
        return true;
    }

    /**
     * Initializes all tickers from the configured engine JSON under {@code defaults.tickers}.
     *
     * @return true if tickers were loaded and initialized.
     */
    public boolean initializeAllTickers() {
        try {
            logger.debug("Current Working Directory: {}", Paths.get("").toAbsolutePath());
            JsonNode configData = readConfigJson();
            JsonNode tickersArray = configData.path("defaults").path("tickers");
            for (JsonNode tickerNode : tickersArray) {
                String ticker = tickerNode.asText();
                logger.info("Initializing ticker: {}", ticker);
                initializeTicker(ticker);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize tickers from config: {}", configLocation, e);
            return false;
        }
        return true;
    }

    /**
     * Initializes a user from the configured engine JSON.
     *
     * <p>Finite mode:
     *
     * <p>Sets cash balance to {@code defaults.defaultBalance} Sets per-ticker inventory from {@code
     * defaults.balances}
     *
     * <p>Infinite mode:
     *
     * <p>Registers the user in {@link UserList} (no fixed cash balance) Sets per-ticker inventory
     * from {@code defaults.balances}
     */
    public boolean initializeUser(String user) {
        try {
            JsonNode configData = readConfigJson();
            if (!userList.getMode()) {
                // Extract and process balances
                JsonNode defaults = configData.path("defaults");
                int defaultBalance = defaults.path("defaultBalance").asInt();
                JsonNode balances = defaults.path("balances");
                logger.info("Default balance: {}", defaultBalance);
                initializeUserBalance(user, defaultBalance);
                Iterator<Entry<String, JsonNode>> fields = balances.fields();
                while (fields.hasNext()) {
                    Entry<String, JsonNode> entry = fields.next();
                    String ticker = entry.getKey();
                    int balance = entry.getValue().asInt();
                    logger.info(
                            "Initializing user ticker: user={} ticker={} balance={}",
                            user,
                            ticker,
                            balance);
                    initializeUserTickerVolume(user, ticker, balance);
                }
            } else {
                JsonNode defaults = configData.path("defaults");
                JsonNode balances = defaults.path("balances");
                Iterator<Entry<String, JsonNode>> fields = balances.fields();
                logger.info("Initializing user in infinite mode: user={}", user);
                userList.initializeUser(user);
                while (fields.hasNext()) {
                    Entry<String, JsonNode> entry = fields.next();
                    String ticker = entry.getKey();
                    int balance = entry.getValue().asInt();
                    logger.info(
                            "Initializing user ticker: user={} ticker={} balance={}",
                            user,
                            ticker,
                            balance);
                    initializeUserTickerVolume(user, ticker, balance);
                }
            }
        } catch (Exception e) {
            logger.error(
                    "Failed to initialize user from config: user={} config={}",
                    user,
                    configLocation,
                    e);
            return false;
        }
        return true;
    }

    /**
     * @return last known price for {@code ticker} (0 if unknown).
     */
    public int getPrice(String ticker) {
        return latestPrice.getOrDefault(ticker, 0);
    }

    /**
     * Sets last known price for {@code ticker} and updates chart tracking.
     *
     * <p>This does not alter order book contents.
     */
    public void setPrice(String ticker, int price) {
        latestPrice.put(ticker, price);
        chartTrackerSingleton.updatePrice(ticker, price);
    }

    /**
     * Sets prices for multiple tickers and clears all order books and users' active orders.
     *
     * <p>Used as an administrative reset.
     */
    public void setPriceClearOrderBook(
            Map<String, Integer> updatedPrices, TaskFuture<String> future) {
        for (String ticker : updatedPrices.keySet()) {
            logger.debug("Updating price and clearing order book for ticker={}", ticker);
            if (!orderBooks.containsKey(ticker)) {
                future.setData("BAD TICKER");
                return;
            }
        }
        for (Map.Entry<String, Integer> entry : updatedPrices.entrySet()) {
            setPrice(entry.getKey(), entry.getValue());
            zeroVolume(orderBooks.get(entry.getKey()), entry.getKey());
        }
        for (String user : userOrders.keySet()) {
            userOrders.get(user).clear();
        }
        future.setData("SUCCESS ALL CLEARED");
    }

    /**
     * @return highest bid price for {@code ticker}, or 0 if no bids or unknown ticker.
     */
    public int getHighestBid(String ticker) {
        if (!orderBooks.containsKey(ticker)) return 0;
        TreeMap<Integer, Deque<Order>> bids = orderBooks.get(ticker).bids;
        if (bids.isEmpty()) {
            return 0;
        }
        return bids.lastKey();
    }

    /**
     * @return lowest ask price for {@code ticker}, or {@link Integer#MAX_VALUE} if no asks.
     */
    public int getLowestAsk(String ticker) {
        if (!orderBooks.containsKey(ticker)) return 0;
        TreeMap<Integer, Deque<Order>> asks = orderBooks.get(ticker).asks;
        if (asks.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return asks.firstKey();
    }

    protected void updateVolume(
            Map<Integer, Integer> volumeMap, int price, int delta, String ticker, Side side) {
        // Maintain aggregated depth per price and publish updates via RecentTrades.
        volumeMap.put(price, volumeMap.getOrDefault(price, 0) + delta);
        if (volumeMap.get(price) <= 0) {
            volumeMap.remove(price);
        }
        int newQuantity = volumeMap.getOrDefault(price, 0);
        // System.out.printf("Price: %d Quantity: %d\n", price, newQuantity);
        RecentTrades.addTrade(ticker, price, newQuantity, side);
    }

    private void zeroVolume(OrderBook orderBook, String ticker) {
        // Emit zero-size updates for all levels, then clear the in-memory book.
        for (int price : orderBook.bidVolumes.keySet())
            RecentTrades.addTrade(ticker, price, 0, Side.BID);
        for (int price : orderBook.askVolumes.keySet())
            RecentTrades.addTrade(ticker, price, 0, Side.ASK);
        orderBook.clearOrderBook();
    }

    private static final class ValidationResult {
        // Lightweight container: a Message code + result detail string.
        private final Message code;
        private final String detail;

        private ValidationResult(Message code, String detail) {
            this.code = code;
            this.detail = detail;
        }
    }

    private ValidationResult validateBidOrder(String user, Order order) {
        // Validates request shape + that the user can afford/reserve the bid.
        // Bots bypass balance/position limits, but still must pass basic sanity checks.
        boolean isBot = bots.containsKey(user);
        if (order.volume <= 0.0 || order.price <= 0.0 || order.status != Status.ACTIVE) {
            logger.debug("Bad bid parameters: user={} order={}", user, order);
            if (order.volume <= 0.0) {
                return new ValidationResult(
                        Message.INVALID_VOLUME,
                        String.format("Invalid volume=%d (must be > 0)", order.volume));
            }
            if (order.price <= 0.0) {
                return new ValidationResult(
                        Message.INVALID_PRICE,
                        String.format("Invalid price=%d (must be > 0)", order.price));
            }
            return new ValidationResult(
                    Message.BAD_INPUT, "Invalid order status (must be ACTIVE for placement)");
        }
        if (!orderBooks.containsKey(order.ticker)) {
            logger.debug("Bid order rejected: unknown ticker={} user={}", order.ticker, user);
            if (orderBooks.isEmpty()) {
                return new ValidationResult(
                        Message.SERVER_MISCONFIGURED,
                        String.format(
                                "Order books are not initialized (check server startup;"
                                        + " hte.config.path=%s)",
                                configLocation));
            }
            return new ValidationResult(
                    Message.UNKNOWN_TICKER,
                    String.format(
                            "Unknown ticker '%s'. Available: %s",
                            order.ticker, String.join(", ", orderBooks.keySet())));
        }
        if (!userList.validUser(user)) {
            logger.debug("Bid order rejected: invalid user={}", user);
            return new ValidationResult(
                    Message.USER_NOT_INITIALIZED,
                    "User not initialized in matching engine yet. Retry shortly.");
        }
        if (isBot) {
            return new ValidationResult(Message.SUCCESS, Message.SUCCESS.getErrorMessage());
        }
        if (!userList.validBidParameters(user, order)) {
            logger.debug(
                    "Bid order rejected: invalid volume parameters user={} order={}", user, order);
            if (userList.getMode()) {
                return new ValidationResult(
                        Message.POSITION_LIMIT_EXCEEDED,
                        String.format(
                                "Position limit exceeded for ticker '%s'. Reduce volume or cancel"
                                        + " existing bids.",
                                order.ticker));
            }
            long balance = userList.getUserBalance(user);
            long required = (long) order.price * (long) order.volume;
            return new ValidationResult(
                    Message.INSUFFICIENT_BALANCE,
                    String.format(
                            "Insufficient balance for bid: required=%d (price=%d*volume=%d),"
                                    + " available=%d",
                            required, order.price, order.volume, balance));
        }
        return new ValidationResult(Message.SUCCESS, Message.SUCCESS.getErrorMessage());
    }

    private ValidationResult validateAskOrder(String user, Order order) {
        // Validates request shape + that the user can reserve enough inventory to sell.
        // Bots bypass inventory/position limits, but still must pass basic sanity checks.
        boolean isBot = bots.containsKey(user);
        if (order.volume <= 0.0 || order.price <= 0.0 || order.status != Status.ACTIVE) {
            logger.debug("Bad ask parameters: user={} order={}", user, order);
            if (order.volume <= 0.0) {
                return new ValidationResult(
                        Message.INVALID_VOLUME,
                        String.format("Invalid volume=%d (must be > 0)", order.volume));
            }
            if (order.price <= 0.0) {
                return new ValidationResult(
                        Message.INVALID_PRICE,
                        String.format("Invalid price=%d (must be > 0)", order.price));
            }
            return new ValidationResult(
                    Message.BAD_INPUT, "Invalid order status (must be ACTIVE for placement)");
        }
        if (!orderBooks.containsKey(order.ticker)) {
            logger.debug("Ask order rejected: unknown ticker={} user={}", order.ticker, user);
            if (orderBooks.isEmpty()) {
                return new ValidationResult(
                        Message.SERVER_MISCONFIGURED,
                        String.format(
                                "Order books are not initialized (check server startup;"
                                        + " hte.config.path=%s)",
                                configLocation));
            }
            return new ValidationResult(
                    Message.UNKNOWN_TICKER,
                    String.format(
                            "Unknown ticker '%s'. Available: %s",
                            order.ticker, String.join(", ", orderBooks.keySet())));
        }
        if (!userList.validUser(user)) {
            logger.debug("Ask order rejected: invalid user={}", user);
            return new ValidationResult(
                    Message.USER_NOT_INITIALIZED,
                    "User not initialized in matching engine yet. Retry shortly.");
        }
        if (isBot) {
            return new ValidationResult(Message.SUCCESS, Message.SUCCESS.getErrorMessage());
        }
        if (!userList.validAskQuantity(user, order.ticker, order.volume)) {
            logger.debug(
                    "Ask order rejected: insufficient sell funds user={} ticker={} volume={}",
                    user,
                    order.ticker,
                    order.volume);
            int available = userList.getValidAskVolume(user, order.ticker);
            return new ValidationResult(
                    Message.INSUFFICIENT_TICKER_BALANCE,
                    String.format(
                            "Insufficient '%s' to sell: requested=%d, available=%d",
                            order.ticker, order.volume, available));
        }
        return new ValidationResult(Message.SUCCESS, Message.SUCCESS.getErrorMessage());
    }

    private Map<String, Object> createLimitOrderResponse(
            double price, int volumeFilled, Message error, String errorMessage, long orderId) {
        // Common response shape for limit orders.
        Map<String, Object> response = new HashMap<>();
        response.put("price", price);
        response.put("volumeFilled", volumeFilled);
        response.put("errorCode", error.getErrorCode());
        response.put("errorMessage", errorMessage);
        response.put("orderId", orderId);
        return response;
    }

    private Map<String, Object> createMarketOrderResponse(
            double price, int volumeFilled, Message error) {
        // Common response shape for market orders.
        Map<String, Object> response = new HashMap<>();
        response.put("price", price);
        response.put("volumeFilled", volumeFilled);
        response.put("errorCode", error.getErrorCode());
        response.put("errorMessage", error.getErrorMessage());
        return response;
    }

    private OrderData processBid(
            Deque<Order> orders, Map<Integer, Integer> askVolumes, Order aggressor) {
        // Matches a BUY aggressor against resting ASKs at one price level.
        OrderData orderData = new OrderData();
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
            } else if (order.volume > aggressor.volume) {
                // Resting order is larger: aggressor fully fills, resting remains ACTIVE.
                int volumeTraded = aggressor.volume;
                updateVolume(askVolumes, order.price, -volumeTraded, order.ticker, Side.ASK);
                userList.adjustUserAskBalance(order.name, order.ticker, -volumeTraded);

                userList.adjustUserBalance(order.name, volumeTraded * order.price);
                userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);

                userList.adjustUserTickerBalance(
                        aggressor.name, order.ticker, volumeTraded, order.price);
                userList.adjustUserTickerBalance(
                        order.name, order.ticker, -volumeTraded, order.price);
                // Update OrderBook to reflect new price of the ticker
                setPrice(order.ticker, order.price);
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
                orderData.linearCombination(order.price, volumeTraded);
            } else {
                // Resting order is smaller/equal: resting fully fills and is removed from queue.
                int volumeTraded = order.volume;
                updateVolume(askVolumes, order.price, -volumeTraded, order.ticker, Side.ASK);
                userList.adjustUserAskBalance(order.name, order.ticker, -volumeTraded);

                userList.adjustUserBalance(order.name, volumeTraded * order.price);
                userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);

                userList.adjustUserTickerBalance(
                        aggressor.name, order.ticker, volumeTraded, order.price);
                userList.adjustUserTickerBalance(
                        order.name, order.ticker, -volumeTraded, order.price);
                aggressor.volume = aggressor.volume - order.volume;

                setPrice(order.ticker, order.price);
                order.volume = 0;
                order.status = Status.FILLED;
                orders.poll();
                orderData.linearCombination(order.price, volumeTraded);
            }
        }
        return orderData;
    }

    public OrderData processAsk(
            Deque<Order> orders, Map<Integer, Integer> bidVolumes, Order aggressor) {
        // Matches a SELL aggressor against resting BIDs at one price level.
        OrderData orderData = new OrderData();
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
            } else if (order.volume > aggressor.volume) {
                // Resting order is larger: aggressor fully fills, resting remains ACTIVE.
                int volumeTraded = aggressor.volume;
                updateVolume(bidVolumes, order.price, -volumeTraded, order.ticker, Side.BID);
                userList.adjustUserBidBalance(order.name, order.ticker, -volumeTraded);

                userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                userList.adjustUserBalance(order.name, -volumeTraded * order.price);

                userList.adjustUserTickerBalance(
                        aggressor.name, order.ticker, -volumeTraded, order.price);
                userList.adjustUserTickerBalance(
                        order.name, order.ticker, volumeTraded, order.price);
                setPrice(order.ticker, order.price);
                // RecentTrades.addTrade(order.name, aggressor.name, order.ticker, order.price,
                // volumeTraded);
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
                orderData.linearCombination(order.price, volumeTraded);
            } else {
                // Resting order is smaller/equal: resting fully fills and is removed from queue.
                int volumeTraded = order.volume;
                updateVolume(bidVolumes, order.price, -volumeTraded, order.ticker, Side.BID);
                userList.adjustUserBidBalance(order.name, order.ticker, -volumeTraded);

                userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                userList.adjustUserBalance(order.name, -volumeTraded * order.price);

                userList.adjustUserTickerBalance(
                        aggressor.name, order.ticker, -volumeTraded, order.price);
                userList.adjustUserTickerBalance(
                        order.name, order.ticker, volumeTraded, order.price);
                setPrice(order.ticker, order.price);
                aggressor.volume = aggressor.volume - order.volume;
                order.volume = 0;
                order.status = Status.FILLED;
                orders.poll();
                orderData.linearCombination(order.price, volumeTraded);
            }
        }
        return orderData;
    }

    /**
     * Places a bid (buy) limit order.
     *
     * <p>If the order crosses the spread, it immediately matches against the best asks until either
     * the order is fully filled or it can no longer match.
     *
     * @param name user/bot name
     * @param order limit order (must be {@link Status#ACTIVE})
     * @return response map suitable for JSON serialization
     */
    public Map<String, Object> bidLimitOrderHandler(String name, Order order) {
        final boolean isBot = bots.containsKey(name);
        final int requestedVolume = order.volume;
        ValidationResult validation = validateBidOrder(name, order);
        if (validation.code != Message.SUCCESS) {
            logger.debug(
                    "Bid limit order rejected due to invalid parameters: user={} order={}",
                    name,
                    order);
            return createLimitOrderResponse(0.0, 0, validation.code, validation.detail, -1);
        }
        TreeMap<Integer, Deque<Order>> asks = orderBooks.get(order.ticker).asks;
        Map<Integer, Integer> askVolumes = orderBooks.get(order.ticker).askVolumes;
        TreeMap<Integer, Deque<Order>> bids = orderBooks.get(order.ticker).bids;
        Map<Integer, Integer> bidVolumes = orderBooks.get(order.ticker).bidVolumes;
        OrderData orderData = new OrderData();
        // validate order ensures that there is sufficient balance
        while (order.volume > 0 && !asks.isEmpty() && asks.firstKey() <= order.price) {
            // Match against current best ask price level.
            Deque<Order> orderList = asks.get(asks.firstKey());
            orderData.add(processBid(orderList, askVolumes, order));
            if (orderList.isEmpty()) {
                asks.pollFirstEntry();
            }
        }
        // System.out.printf("BID LIMIT ORDER Remaining Volume to be placed on the
        // orderbook: %d\n",
        // order.volume);
        String msg =
                String.format(
                        "SUCCESS! BID LIMIT ORDER Remaining Volume to be placed on the orderbook:"
                                + " %d\n",
                        order.volume);
        if (orderData.volume > 0) {
            // Convert accumulated price*volume into VWAP.
            orderData.price /= orderData.volume;
        }
        if (order.volume > 0) {
            // Remaining volume becomes a resting order on the bid side.
            order.status = Status.ACTIVE;
            bids.computeIfAbsent(order.price, k -> new LinkedList<>()).add(order);
            updateVolume(bidVolumes, order.price, order.volume, order.ticker, Side.BID);
            // userList.adjustUserBalance(name, -order.price * order.volume);
            userList.adjustUserBidBalance(name, order.ticker, order.volume);
            orderID++;
            if (userOrders.containsKey(order.name)) {
                userOrders.get(order.name).put(orderID, order);
            } else {
                userOrders.put(order.name, new HashMap<>());
                userOrders.get(order.name).put(orderID, order);
            }
            logger.info(
                    "Order placed successfully: actorType={} user={} side=BID type=LIMIT ticker={}"
                            + " limitPrice={} requestedVolume={} filledVolume={} remainingVolume={}"
                            + " vwapPrice={} orderId={}",
                    isBot ? "BOT" : "USER",
                    name,
                    order.ticker,
                    order.price,
                    requestedVolume,
                    (int) orderData.volume,
                    order.volume,
                    orderData.price,
                    orderID);
            return createLimitOrderResponse(
                    orderData.price, (int) orderData.volume, Message.SUCCESS, msg, orderID);
        } else {
            order.status = Status.FILLED;
        }
        logger.info(
                "Order placed successfully: actorType={} user={} side=BID type=LIMIT ticker={}"
                        + " limitPrice={} requestedVolume={} filledVolume={} remainingVolume=0"
                        + " vwapPrice={} orderId=0",
                isBot ? "BOT" : "USER",
                name,
                order.ticker,
                order.price,
                requestedVolume,
                (int) orderData.volume,
                orderData.price);
        return createLimitOrderResponse(
                orderData.price, (int) orderData.volume, Message.SUCCESS, msg, 0);
    }

    public long bidLimitOrder(String name, Order order) {
        // Convenience overload: return only the created orderId.
        return (long) bidLimitOrderHandler(name, order).get("orderId");
    }

    public void bidLimitOrder(String name, Order order, TaskFuture<String> future) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse =
                    objectMapper.writeValueAsString(bidLimitOrderHandler(name, order));
            future.setData(jsonResponse);
        } catch (Exception e) {
            logger.error(
                    "Failed to serialize bid limit order response: user={} order={}",
                    name,
                    order,
                    e);
        }
    }

    /**
     * Places an ask (sell) limit order.
     *
     * <p>If the order crosses the spread, it immediately matches against the best bids until either
     * the order is fully filled or it can no longer match.
     */
    public Map<String, Object> askLimitOrderHandler(String name, Order order) {
        final boolean isBot = bots.containsKey(name);
        final int requestedVolume = order.volume;
        ValidationResult validation = validateAskOrder(name, order);
        if (validation.code != Message.SUCCESS) {
            return createLimitOrderResponse(0, 0, validation.code, validation.detail, -1);
        }
        TreeMap<Integer, Deque<Order>> asks = orderBooks.get(order.ticker).asks;
        Map<Integer, Integer> askVolumes = orderBooks.get(order.ticker).askVolumes;
        TreeMap<Integer, Deque<Order>> bids = orderBooks.get(order.ticker).bids;
        Map<Integer, Integer> bidVolumes = orderBooks.get(order.ticker).bidVolumes;
        OrderData orderData = new OrderData();
        while (order.volume > 0 && !bids.isEmpty() && bids.lastKey() >= order.price) {
            // Match against current best bid price level.
            Deque<Order> orderList = bids.get(bids.lastKey());
            orderData.add(processAsk(orderList, bidVolumes, order));
            if (orderList.isEmpty()) {
                bids.pollLastEntry();
            }
        }
        // System.out.printf("ASK LIMIT ORDER Remaining Volume to be placed on the
        // orderbook: %d\n",
        // order.volume);
        String msg =
                String.format(
                        "SUCCESS! ASK LIMIT ORDER Remaining Volume to be placed on the orderbook:"
                                + " %d\n",
                        order.volume);
        if (orderData.volume > 0) {
            // Convert accumulated price*volume into VWAP.
            orderData.price /= orderData.volume;
        }
        if (order.volume > 0) {
            // Remaining volume becomes a resting order on the ask side.
            order.status = Status.ACTIVE;
            asks.computeIfAbsent(order.price, k -> new LinkedList<>()).add(order);
            orderID++;
            updateVolume(askVolumes, order.price, order.volume, order.ticker, Side.ASK);
            // userList.adjustUserTickerBalance(order.name, order.ticker, -order.volume);
            userList.adjustUserAskBalance(order.name, order.ticker, order.volume);
            if (userOrders.containsKey(order.name)) {
                userOrders.get(order.name).put(orderID, order);
            } else {
                userOrders.put(order.name, new HashMap<>());
                userOrders.get(order.name).put(orderID, order);
            }
            logger.info(
                    "Order placed successfully: actorType={} user={} side=ASK type=LIMIT ticker={}"
                            + " limitPrice={} requestedVolume={} filledVolume={} remainingVolume={}"
                            + " vwapPrice={} orderId={}",
                    isBot ? "BOT" : "USER",
                    name,
                    order.ticker,
                    order.price,
                    requestedVolume,
                    (int) orderData.volume,
                    order.volume,
                    orderData.price,
                    orderID);
            return createLimitOrderResponse(
                    orderData.price, (int) orderData.volume, Message.SUCCESS, msg, orderID);
        } else {
            order.status = Status.FILLED;
        }
        logger.info(
                "Order placed successfully: actorType={} user={} side=ASK type=LIMIT ticker={}"
                        + " limitPrice={} requestedVolume={} filledVolume={} remainingVolume=0"
                        + " vwapPrice={} orderId=0",
                isBot ? "BOT" : "USER",
                name,
                order.ticker,
                order.price,
                requestedVolume,
                (int) orderData.volume,
                orderData.price);
        return createLimitOrderResponse(
                orderData.price, (int) orderData.volume, Message.SUCCESS, msg, 0);
    }

    public long askLimitOrder(String name, Order order) {
        // Convenience overload: return only the created orderId.
        return (long) askLimitOrderHandler(name, order).get("orderId");
    }

    public void askLimitOrder(String name, Order order, TaskFuture<String> future) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse =
                    objectMapper.writeValueAsString(askLimitOrderHandler(name, order));
            future.setData(jsonResponse);
        } catch (Exception e) {
            logger.error(
                    "Failed to serialize ask limit order response: user={} order={}",
                    name,
                    order,
                    e);
        }
    }

    protected Map<Integer, Deque<Order>> getBids(String ticker) {
        // Internal helper for tests/controllers.
        return orderBooks.get(ticker).bids;
    }

    protected Map<Integer, Deque<Order>> getAsks(String ticker) {
        // Internal helper for tests/controllers.
        return orderBooks.get(ticker).asks;
    }

    /**
     * Returns aggregated bid sizes per price level for {@code ticker}.
     *
     * <p>Used for building order book depth views.
     */
    public List<PriceLevel> getBidPriceLevels(String ticker) {
        Map<Integer, Integer> bidVolumes = orderBooks.get(ticker).bidVolumes;
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : bidVolumes.entrySet()) {
            priceLevels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }
        return priceLevels;
    }

    /**
     * Returns aggregated ask sizes per price level for {@code ticker}.
     *
     * <p>Used for building order book depth views.
     */
    public List<PriceLevel> getAskPriceLevels(String ticker) {
        Map<Integer, Integer> askVolumes = orderBooks.get(ticker).askVolumes;
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : askVolumes.entrySet()) {
            priceLevels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }
        return priceLevels;
    }

    /**
     * Fetches a user's order by order id.
     *
     * @return the order, or {@code null} if user or order is not found.
     */
    public Order getOrder(String userId, long orderId) {
        if (!userList.validUser(userId)) return null;
        Map<Long, Order> orders = userOrders.get(userId);
        if (!orders.containsKey(orderId)) return null;
        return orders.get(orderId);
    }

    /**
     * Cancels an active order.
     *
     * <p>This updates reserved balances/volumes and removes the order from the user's active map.
     */
    public boolean removeOrder(String userId, long orderId) {
        // Cancels an ACTIVE order and releases any reserved capacity.
        if (!userList.validUser(userId)) return false;
        Map<Long, Order> orders = userOrders.get(userId);
        if (orders != null) {
            if (orders.containsKey(orderId) && orders.get(orderId).status == Status.ACTIVE) {
                Order order = orders.get(orderId);
                orders.get(orderId).status = Status.CANCELLED;
                if (order.side == Side.BID) {
                    // userList.adjustUserBalance(userId, order.price * order.volume);
                    userList.adjustUserBidBalance(userId, order.ticker, -order.volume);
                    Map<Integer, Integer> bidVolumes = orderBooks.get(order.ticker).bidVolumes;
                    updateVolume(bidVolumes, order.price, -order.volume, order.ticker, Side.BID);
                } else {
                    // userList.adjustUserTickerBalance(userId, order.ticker, order.volume);
                    userList.adjustUserAskBalance(userId, order.ticker, -order.volume);
                    Map<Integer, Integer> askVolumes = orderBooks.get(order.ticker).askVolumes;
                    updateVolume(askVolumes, order.price, -order.volume, order.ticker, Side.ASK);
                }
                orders.remove(orderId);
                return true;
            }
        }
        return false;
    }

    // todo: verify correctness of messaging
    public boolean removeOrder(String userId, long orderId, TaskFuture<String> future) {
        /*
         * Async cancellation variant that returns a JSON-ish Message string.
         */
        if (!userList.validUser(userId)) {
            future.setData(Message.USER_NOT_INITIALIZED.toString());
            return false;
        }
        if (orderId <= 0) {
            future.setData(Message.INVALID_ORDER_ID.toString());
            return false;
        }
        Map<Long, Order> orders = userOrders.get(userId);
        if (orders != null) {
            if (orders.containsKey(orderId) && orders.get(orderId).status == Status.ACTIVE) {
                Order order = orders.get(orderId);
                orders.get(orderId).status = Status.CANCELLED;
                if (order.side == Side.BID) {
                    // userList.adjustUserBalance(userId, order.price * order.volume);
                    userList.adjustUserBidBalance(userId, order.ticker, -order.volume);
                    Map<Integer, Integer> bidVolumes = orderBooks.get(order.ticker).bidVolumes;
                    updateVolume(bidVolumes, order.price, -order.volume, order.ticker, Side.BID);
                } else {
                    // userList.adjustUserTickerBalance(userId, order.ticker, order.volume);
                    userList.adjustUserAskBalance(userId, order.ticker, -order.volume);
                    Map<Integer, Integer> askVolumes = orderBooks.get(order.ticker).askVolumes;
                    updateVolume(askVolumes, order.price, -order.volume, order.ticker, Side.ASK);
                }
                future.setData(
                        String.format(
                                "{\"errorCode\": %d, \"errorMessage\": \"%s\"}",
                                Message.SUCCESS.getErrorCode(),
                                String.format(
                                        "removed order with properties - id: %d, volume: %d, ",
                                        orderId, orders.remove(orderId).volume)));
                return true;
            }
        }
        future.setData(Message.ORDER_NOT_FOUND.toString());
        // future.setData("Invalid OrderID");
        return false;
    }

    /**
     * Cancels all of a user's active orders.
     *
     * <p>Silent no-op if the user is invalid or has no orders.
     */
    public void removeAll(String userId) {
        // Cancel all ACTIVE orders for this user.
        if (!userList.validUser(userId)) {
            return;
        }
        // Retrieve user's orders
        Map<Long, Order> orders = userOrders.get(userId);
        if (orders == null || orders.isEmpty()) {
            return; // No orders to remove
        }

        // Iterate through all orders and remove each
        for (Long orderId : new ArrayList<>(orders.keySet())) {
            removeOrder(userId, orderId);
        }
    }

    public void removeAll(String userId, TaskFuture<String> future) {
        if (!userList.validUser(userId)) {
            future.setData(Message.USER_NOT_INITIALIZED.toString());
            return;
        }

        // Retrieve user's orders
        Map<Long, Order> orders = userOrders.get(userId);
        if (orders == null || orders.isEmpty()) {
            future.setData(
                    String.format(
                            "{\"errorCode\": %d, \"errorMessage\": \"%s\"}",
                            Message.SUCCESS.getErrorCode(), "No active orders to remove"));
            return; // No orders to remove
        }

        int volumeRemoved = 0;

        // Iterate through all orders and remove each
        for (Long orderId : new ArrayList<>(orders.keySet())) {
            int vol = orders.get(orderId).volume;
            boolean removed = removeOrder(userId, orderId);
            if (removed) {
                volumeRemoved += vol;
            }
        }

        future.setData(
                String.format(
                        "{\"errorCode\": %d, \"errorMessage\": \"%s\"}",
                        Message.SUCCESS.getErrorCode(),
                        String.format("Removed total volume of %d", volumeRemoved)));
    }

    /**
     * Matches a market order against the opposite side of the book.
     *
     * <p>Important: For non-bot users, the aggressor may be capped by available funds/inventory via
     * {@link UserList#getValidBidVolume(String, String, int)} and {@link
     * UserList#getValidAskVolume(String, String)}.
     *
     * @return aggregated execution data (total volume + linear-combination price accumulator).
     */
    public OrderData processMarketOrder(
            Deque<Order> orders, Map<Integer, Integer> volumeMap, Order aggressor, Side side) {
        // Matches a market aggressor against resting orders at one price level.
        OrderData orderData = new OrderData();
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
                continue;
            }
            // 2 Cases:
            // First Case: Finite Stack - only sell what you can own
            int aggressorVolume = aggressor.volume;
            // IMPORTANT: bots bypass finite-mode balance/inventory caps.
            // Otherwise, a bot with 0 balance would have aggressorVolume=0, causing
            // the outer market-order loop to spin forever without making progress.
            if (!bots.containsKey(aggressor.name)) {
                if (side == Side.BID) {
                    aggressorVolume =
                            Math.min(
                                    aggressorVolume,
                                    userList.getValidBidVolume(
                                            aggressor.name, order.ticker, order.price));
                } else if (side == Side.ASK) {
                    aggressorVolume =
                            Math.min(
                                    aggressorVolume,
                                    userList.getValidAskVolume(aggressor.name, order.ticker));
                }
            }
            // If the aggressor cannot trade any volume (e.g., insufficient balance/inventory),
            // stop processing and let the caller decide how to report it.
            if (aggressorVolume <= 0) {
                break;
            }
            if (order.volume > aggressorVolume) {
                int volumeTraded = aggressorVolume;
                int tradePrice = order.price;
                if (side == Side.BID) {
                    userList.adjustUserAskBalance(order.name, order.ticker, -volumeTraded);

                    userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, volumeTraded * order.price);
                    // Add volume to the aggressor's ticker balance, since it is buying
                    userList.adjustUserTickerBalance(
                            aggressor.name, order.ticker, volumeTraded, order.price);
                    userList.adjustUserTickerBalance(
                            order.name, order.ticker, -volumeTraded, order.price);
                    // Update the ask volume map and the ask if it is a bid order
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.ASK);
                } else {
                    userList.adjustUserBidBalance(order.name, order.ticker, -volumeTraded);

                    userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, -volumeTraded * order.price);
                    // Remove volume from the aggressor's ticker balance and add to the order's
                    // balance
                    userList.adjustUserTickerBalance(
                            aggressor.name, order.ticker, -volumeTraded, order.price);
                    userList.adjustUserTickerBalance(
                            order.name, order.ticker, volumeTraded, order.price);
                    // Update the bid volume map and the bid if it is a bid order
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.BID);
                }
                setPrice(order.ticker, tradePrice);
                order.volume -= aggressorVolume;
                orderData.linearCombination(tradePrice, volumeTraded);
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
                int volumeTraded = order.volume;
                int tradePrice = order.price;
                if (side == Side.BID) {
                    userList.adjustUserAskBalance(order.name, order.ticker, -volumeTraded);

                    userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, volumeTraded * order.price);
                    userList.adjustUserTickerBalance(
                            aggressor.name, order.ticker, volumeTraded, order.price);
                    userList.adjustUserTickerBalance(
                            order.name, order.ticker, -volumeTraded, order.price);
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.ASK);
                } else {
                    userList.adjustUserBidBalance(order.name, order.ticker, -volumeTraded);

                    userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, -volumeTraded * order.price);
                    userList.adjustUserTickerBalance(
                            aggressor.name, order.ticker, -volumeTraded, order.price);
                    userList.adjustUserTickerBalance(
                            order.name, order.ticker, volumeTraded, order.price);
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.BID);
                }
                setPrice(order.ticker, tradePrice);
                orderData.linearCombination(tradePrice, volumeTraded);
                aggressor.volume -= order.volume;
                order.volume = 0;
                order.status = Status.FILLED;
                orders.poll();
            }
        }
        return orderData;
    }

    /**
     * Executes a buy market order.
     *
     * <p>Response semantics:
     *
     * <p>If nothing fills: returns an error (e.g., {@link Message#NO_LIQUIDITY} or {@link
     * Message#INSUFFICIENT_BALANCE}). If some volume fills but not all: returns {@link
     * Message#PARTIAL_FILL} (treated as success via {@code errorCode==0}). If fully filled: returns
     * {@link Message#SUCCESS}.
     */
    public Map<String, Object> bidMarketOrderHandler(String name, String ticker, int volume) {
        if (!userList.validUser(name) && !bots.containsKey(name)) {
            // System.out.println("Invalid");
            return createMarketOrderResponse(0.0, 0, Message.AUTHENTICATION_FAILED);
        }
        if (volume <= 0) {
            return createMarketOrderResponse(0.0, 0, Message.INVALID_VOLUME);
        }
        if (!orderBooks.containsKey(ticker)) {
            return createMarketOrderResponse(0.0, 0, Message.UNKNOWN_TICKER);
        }
        int requestedVolume = volume;
        OrderData orderData = new OrderData();
        Order marketOrder =
                new Order(
                        name,
                        ticker,
                        0,
                        volume,
                        Side.BID,
                        Status.ACTIVE); // Price is 0 for market orders
        // int volumeFilled = 0;
        TreeMap<Integer, Deque<Order>> asks = orderBooks.get(ticker).asks;
        Map<Integer, Integer> askVolumes = orderBooks.get(ticker).askVolumes;

        if (asks.isEmpty()) {
            return createMarketOrderResponse(0.0, 0, Message.NO_LIQUIDITY);
        }
        if (!bots.containsKey(name)
                && userList.getValidBidVolume(name, ticker, asks.firstKey()) <= 0) {
            return createMarketOrderResponse(0.0, 0, Message.INSUFFICIENT_BALANCE);
        }

        while (marketOrder.volume > 0 && !asks.isEmpty()) {
            // Match at current best ask price level.
            Deque<Order> orderList = asks.get(asks.firstKey());
            orderData.add(processMarketOrder(orderList, askVolumes, marketOrder, Side.BID));
            if (orderList.isEmpty()) {
                asks.pollFirstEntry();
            }
            if (!bots.containsKey(name)
                    && marketOrder.volume > 0
                    && !asks.isEmpty()
                    && userList.getValidBidVolume(name, ticker, asks.firstKey()) <= 0) {
                // Cannot afford even 1 unit at the next best ask price.
                break;
            }
        }
        if (orderData.volume > 0) {
            // Convert accumulated price*volume into VWAP.
            orderData.price /= orderData.volume;
        }
        if (marketOrder.volume > 0) {
            // Cancel any remaining volume
            marketOrder.status = Status.CANCELLED;
            marketOrder.volume = 0;
        } else {
            marketOrder.status = Status.FILLED;
        }

        if (orderData.volume <= 0) {
            if (asks.isEmpty()) {
                return createMarketOrderResponse(0.0, 0, Message.NO_LIQUIDITY);
            }
            if (!bots.containsKey(name)
                    && userList.getValidBidVolume(name, ticker, asks.firstKey()) <= 0) {
                return createMarketOrderResponse(0.0, 0, Message.INSUFFICIENT_BALANCE);
            }
            return createMarketOrderResponse(0.0, 0, Message.BAD_INPUT);
        }

        if ((int) orderData.volume < requestedVolume) {
            logger.info(
                    "Order placed successfully: actorType={} user={} side=BID type=MARKET ticker={}"
                            + " requestedVolume={} filledVolume={} remainingVolume={} vwapPrice={}"
                            + " status=PARTIAL_FILL",
                    bots.containsKey(name) ? "BOT" : "USER",
                    name,
                    ticker,
                    requestedVolume,
                    (int) orderData.volume,
                    requestedVolume - (int) orderData.volume,
                    orderData.price);
            return createMarketOrderResponse(
                    orderData.price, (int) orderData.volume, Message.PARTIAL_FILL);
        }

        logger.info(
                "Order placed successfully: actorType={} user={} side=BID type=MARKET ticker={}"
                        + " requestedVolume={} filledVolume={} remainingVolume=0 vwapPrice={}"
                        + " status=SUCCESS",
                bots.containsKey(name) ? "BOT" : "USER",
                name,
                ticker,
                requestedVolume,
                (int) orderData.volume,
                orderData.price);
        return createMarketOrderResponse(orderData.price, (int) orderData.volume, Message.SUCCESS);
    }

    public int bidMarketOrder(String name, String ticker, int volume) {
        // Convenience overload: return only the filled volume.
        return (int) bidMarketOrderHandler(name, ticker, volume).get("volumeFilled");
    }

    public void bidMarketOrder(String name, String ticker, int volume, TaskFuture<String> future) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse =
                    objectMapper.writeValueAsString(bidMarketOrderHandler(name, ticker, volume));
            future.setData(jsonResponse);
        } catch (Exception e) {
            logger.error(
                    "Failed to serialize bid market order response: user={} ticker={} volume={}",
                    name,
                    ticker,
                    volume,
                    e);
        }
    }

    /**
     * Executes a sell market order.
     *
     * <p>See {@link #bidMarketOrderHandler(String, String, int)} for response semantics.
     */
    public Map<String, Object> askMarketOrderHandler(String name, String ticker, int volume) {
        if (!userList.validUser(name) && !bots.containsKey(name)) {
            return createMarketOrderResponse(0.0, 0, Message.AUTHENTICATION_FAILED);
        }
        if (volume <= 0) {
            return createMarketOrderResponse(0.0, 0, Message.INVALID_VOLUME);
        }
        if (!orderBooks.containsKey(ticker)) {
            return createMarketOrderResponse(0.0, 0, Message.UNKNOWN_TICKER);
        }
        if (!userList.validAskQuantity(name, ticker, volume) && !bots.containsKey(name)) {
            return createMarketOrderResponse(0.0, 0, Message.INSUFFICIENT_TICKER_BALANCE);
        }
        int requestedVolume = volume;
        OrderData orderData = new OrderData();
        Order marketOrder =
                new Order(
                        name,
                        ticker,
                        0,
                        volume,
                        Side.ASK,
                        Status.ACTIVE); // Price is 0 for market orders
        // int volumeFilled = 0;
        TreeMap<Integer, Deque<Order>> bids = orderBooks.get(ticker).bids;
        Map<Integer, Integer> bidVolumes = orderBooks.get(ticker).bidVolumes;

        if (bids.isEmpty()) {
            return createMarketOrderResponse(0.0, 0, Message.NO_LIQUIDITY);
        }

        while (marketOrder.volume > 0 && !bids.isEmpty()) {
            // Match at current best bid price level.
            Deque<Order> orderList = bids.get(bids.lastKey());
            orderData.add(processMarketOrder(orderList, bidVolumes, marketOrder, Side.ASK));
            if (orderList.isEmpty()) {
                bids.pollLastEntry();
            }
            if (!bots.containsKey(name)
                    && marketOrder.volume > 0
                    && userList.getValidAskVolume(name, ticker) <= 0) {
                // Cannot sell any additional volume.
                break;
            }
        }
        if (orderData.volume > 0) {
            // Convert accumulated price*volume into VWAP.
            orderData.price /= orderData.volume;
        }
        if (marketOrder.volume > 0) {
            // Cancel any remaining volume
            marketOrder.status = Status.CANCELLED;
            marketOrder.volume = 0;
        } else {
            marketOrder.status = Status.FILLED;
        }

        if (orderData.volume <= 0) {
            if (bids.isEmpty()) {
                return createMarketOrderResponse(0.0, 0, Message.NO_LIQUIDITY);
            }
            if (!bots.containsKey(name) && userList.getValidAskVolume(name, ticker) <= 0) {
                return createMarketOrderResponse(0.0, 0, Message.INSUFFICIENT_TICKER_BALANCE);
            }
            return createMarketOrderResponse(0.0, 0, Message.BAD_INPUT);
        }

        if ((int) orderData.volume < requestedVolume) {
            logger.info(
                    "Order placed successfully: actorType={} user={} side=ASK type=MARKET ticker={}"
                            + " requestedVolume={} filledVolume={} remainingVolume={} vwapPrice={}"
                            + " status=PARTIAL_FILL",
                    bots.containsKey(name) ? "BOT" : "USER",
                    name,
                    ticker,
                    requestedVolume,
                    (int) orderData.volume,
                    requestedVolume - (int) orderData.volume,
                    orderData.price);
            return createMarketOrderResponse(
                    orderData.price, (int) orderData.volume, Message.PARTIAL_FILL);
        }

        logger.info(
                "Order placed successfully: actorType={} user={} side=ASK type=MARKET ticker={}"
                        + " requestedVolume={} filledVolume={} remainingVolume=0 vwapPrice={}"
                        + " status=SUCCESS",
                bots.containsKey(name) ? "BOT" : "USER",
                name,
                ticker,
                requestedVolume,
                (int) orderData.volume,
                orderData.price);
        return createMarketOrderResponse(orderData.price, (int) orderData.volume, Message.SUCCESS);
    }

    public int askMarketOrder(String name, String ticker, int volume) {
        // Convenience overload: return only the filled volume.
        return (int) askMarketOrderHandler(name, ticker, volume).get("volumeFilled");
    }

    public void askMarketOrder(String name, String ticker, int volume, TaskFuture<String> future) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse =
                    objectMapper.writeValueAsString(askMarketOrderHandler(name, ticker, volume));
            future.setData(jsonResponse);
        } catch (Exception e) {
            logger.error(
                    "Failed to serialize ask market order response: user={} ticker={} volume={}",
                    name,
                    ticker,
                    volume,
                    e);
        }
    }

    /**
     * Returns a JSON string containing user balances, inventory, PnL inputs, and active orders
     * grouped by ticker.
     */
    public String getUserDetails(String username) {
        // Combines UserList details with ACTIVE orders grouped by ticker.
        ObjectNode userListDetails = userList.getUserDetailsAsJson(username, latestPrice);

        // Organize active orders by ticker
        ObjectNode ordersByTicker = JsonNodeFactory.instance.objectNode();

        if (userOrders.containsKey(username)) {
            Map<Long, Order> orders = userOrders.get(username);

            for (Map.Entry<Long, Order> entry : orders.entrySet()) {
                Order order = entry.getValue();

                if (order.status == Status.ACTIVE) {
                    ObjectNode orderDetails = JsonNodeFactory.instance.objectNode();
                    orderDetails.put("orderId", entry.getKey());
                    orderDetails.put("price", order.price);
                    orderDetails.put("volume", order.volume);
                    orderDetails.put("side", order.side.toString());

                    ArrayNode tickerOrders = (ArrayNode) ordersByTicker.get(order.ticker);
                    if (tickerOrders == null) {
                        tickerOrders = JsonNodeFactory.instance.arrayNode();
                        ordersByTicker.set(order.ticker, tickerOrders);
                    }
                    tickerOrders.add(orderDetails);
                }
            }
        }

        // Ensure ordersByTicker is added even if empty
        userListDetails.set("Orders", ordersByTicker);
        return userListDetails.toString();
    }

    /** Asynchronously computes leaderboard entries using current {@code latestPrice} marks. */
    public void getLeaderboard(TaskFuture<ArrayList<LeaderboardEntry>> future) {
        future.setData(userList.getLeaderboard(latestPrice));
        future.markAsComplete();
    }

    /** Applies an auction bid by subtracting {@code bid} from the user's cash balance. */
    public void executeAuction(String user, int bid) {
        // Auction bids are represented as a direct cash balance adjustment.
        userList.adjustUserBalance(user, -bid);
    }
}
