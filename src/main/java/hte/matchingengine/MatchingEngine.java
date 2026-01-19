package hte.matchingengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import hte.common.ChartTrackerSingleton;
import hte.common.Message;
import hte.common.TaskFuture;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// todo: get rid of usage of JSON library

public class MatchingEngine {
    private static final Logger logger = LoggerFactory.getLogger(MatchingEngine.class);
    private Map<String, OrderBook> orderBooks = new HashMap<>();
    private Map<String, Map<Long, Order>> userOrders = new HashMap<>(); // UserName, OrderId, Order
    private Map<String, Integer> latestPrice = new HashMap<>(); // For PnL
    private Map<String, Integer> bots = new HashMap<>();
    private ChartTrackerSingleton chartTrackerSingleton = ChartTrackerSingleton.getInstance();
    private UserList userList = new UserList();
    private long orderID = 0;

    public MatchingEngine() {
        userList.setInfinite(false);
    }

    public MatchingEngine(boolean initialize) {
        if (initialize) initializeGameMode();
    }

    public MatchingEngine(int positionLimit) {
        userList.setInfinite(true);
        userList.setPositionLimit(positionLimit);
    }

    public String serializeOrderBooks() {
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
        try {
            FileReader reader = new FileReader("config.json");
            StringBuilder content = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                content.append((char) i);
            }
            reader.close();

            // Parse JSON content
            JSONObject configData = new JSONObject(content.toString());
            String mode = configData.getString("mode");
            if (mode.equals("finite")) userList.setInfinite(false);
            else {
                userList.setInfinite(true);
                JSONObject defaults = configData.getJSONObject("defaults");
                int positionLimit = defaults.getInt("positionLimit");
                userList.setPositionLimit(positionLimit);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize game mode from config.json", e);
        }
    }

    public boolean initializeBot(String username) {
        bots.put(username, 0);
        initializeUserBalance(username, 0);
        for (String ticker : orderBooks.keySet()) {
            initializeUserTickerVolume(username, ticker, 0);
        }
        return true;
    }

    public boolean initializeUserBalance(String username, int balance) {
        logger.info("Initializing user balance: username={} balance={}", username, balance);
        return userList.initializeUser(username, balance);
    }

    public boolean initializeUserTickerVolume(String username, String ticker, int volume) {
        logger.info(
                "Initializing user ticker volume: username={} ticker={} volume={}",
                username,
                ticker,
                volume);
        return userList.initializeUserQuantity(username, ticker, volume);
    }

    public int getUserBalance(String username) {
        return (int) userList.getUserBalance(username);
    }

    public long getPnl(String username) {
        return userList.getUnrealizedPnl(username, latestPrice);
    }

    public ArrayList<PriceChange> getRecentTrades() {
        ArrayList<PriceChange> recentTrades = RecentTrades.getRecentTrades();
        // for (Trade trade : recentTrades)
        /// System.out.println(trade);
        return recentTrades;
    }

    public int getTickerBalance(String username, String ticker) {
        return userList.getUserVolume(username, ticker);
    }

    public boolean initializeTicker(String ticker) {
        if (orderBooks.containsKey(ticker)) {
            return false;
        }
        orderBooks.put(ticker, new OrderBook());
        return true;
    }

    // todo: test and replace
    public boolean alternativeInitializeAllTickers() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            FileReader reader = new FileReader("config.json");
            OrderbookConfig configData = mapper.readerFor(OrderbookConfig.class).readValue(reader);
            reader.close();

            for (String ticker : configData.getDefaults().getTickers()) {
                logger.info("Initializing ticker: {}", ticker);
                initializeTicker(ticker);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize tickers from config.json", e);
            return false;
        }
        return true;
    }

    public boolean initializeAllTickers() {
        try {
            // Read the JSON file
            logger.debug("Current Working Directory: {}", Paths.get("").toAbsolutePath());
            FileReader reader = new FileReader("config.json");
            StringBuilder content = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                content.append((char) i);
            }
            reader.close();
            JSONObject configData = new JSONObject(content.toString());
            JSONArray tickersArray = configData.getJSONObject("defaults").getJSONArray("tickers");
            for (int j = 0; j < tickersArray.length(); j++) {
                String ticker = (String) tickersArray.getString(j);
                logger.info("Initializing ticker: {}", ticker);
                initializeTicker(ticker);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize tickers from config.json", e);
            return false;
        }
        return true;
    }

    // todo test and replace
    public boolean alternativeInitializeUser(String user) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            FileReader reader = new FileReader("config.json");
            OrderbookConfig configData = mapper.readerFor(OrderbookConfig.class).readValue(reader);
            reader.close();

            logger.info(
                    "Default balance from config.json: {}",
                    configData.getDefaults().getDefaultBalance());
            for (String key : configData.getDefaults().getBalances().keySet()) {
                logger.info(
                        "Initializing user ticker: user={} ticker={} balance={}",
                        user,
                        key,
                        configData.getDefaults().getBalances().get(key));
                initializeUserTickerVolume(
                        user, key, configData.getDefaults().getBalances().get(key));
            }
        } catch (Exception e) {
            logger.error("Failed to initialize user from config.json: user={}", user, e);
            return false;
        }
        return true;
    }

    public boolean initializeUser(String user) {
        try {
            // Read the JSON file
            FileReader reader = new FileReader("config.json");
            StringBuilder content = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                content.append((char) i);
            }
            reader.close();

            // Parse JSON content
            JSONObject configData = new JSONObject(content.toString());
            if (!userList.getMode()) {
                // Extract and process balances
                JSONObject defaults = configData.getJSONObject("defaults");
                int defaultBalance = defaults.getInt("defaultBalance");
                JSONObject balances = defaults.getJSONObject("balances");
                logger.info("Default balance: {}", defaultBalance);
                initializeUserBalance(user, defaultBalance);
                Iterator<String> keys = balances.keys();
                while (keys.hasNext()) {
                    String ticker = keys.next();
                    int balance = balances.getInt(ticker);
                    logger.info(
                            "Initializing user ticker: user={} ticker={} balance={}",
                            user,
                            ticker,
                            balance);
                    initializeUserTickerVolume(user, ticker, balance);
                }
            } else {
                JSONObject defaults = configData.getJSONObject("defaults");
                JSONObject balances = defaults.getJSONObject("balances");
                Iterator<String> keys = balances.keys();
                logger.info("Initializing user in infinite mode: user={}", user);
                userList.initializeUser(user);
                while (keys.hasNext()) {
                    String ticker = keys.next();
                    int balance = balances.getInt(ticker);
                    logger.info(
                            "Initializing user ticker: user={} ticker={} balance={}",
                            user,
                            ticker,
                            balance);
                    initializeUserTickerVolume(user, ticker, balance);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to initialize user from config.json: user={}", user, e);
            return false;
        }
        return true;
    }

    public int getPrice(String ticker) {
        return latestPrice.getOrDefault(ticker, 0);
    }

    public void setPrice(String ticker, int price) {
        latestPrice.put(ticker, price);
        chartTrackerSingleton.updatePrice(ticker, price);
    }

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

    public int getHighestBid(String ticker) {
        if (!orderBooks.containsKey(ticker)) return 0;
        TreeMap<Integer, Deque<Order>> bids = orderBooks.get(ticker).bids;
        if (bids.isEmpty()) {
            return 0;
        }
        return bids.lastKey();
    }

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
        volumeMap.put(price, volumeMap.getOrDefault(price, 0) + delta);
        if (volumeMap.get(price) <= 0) {
            volumeMap.remove(price);
        }
        int newQuantity = volumeMap.getOrDefault(price, 0);
        // System.out.printf("Price: %d Quantity: %d\n", price, newQuantity);
        RecentTrades.addTrade(ticker, price, newQuantity, side);
    }

    private void zeroVolume(OrderBook orderBook, String ticker) {
        for (int price : orderBook.bidVolumes.keySet())
            RecentTrades.addTrade(ticker, price, 0, Side.BID);
        for (int price : orderBook.askVolumes.keySet())
            RecentTrades.addTrade(ticker, price, 0, Side.ASK);
        orderBook.clearOrderBook();
    }

    private static final class ValidationResult {
        private final Message code;
        private final String detail;

        private ValidationResult(Message code, String detail) {
            this.code = code;
            this.detail = detail;
        }
    }

    private ValidationResult validateBidOrder(String user, Order order) {
        if (bots.containsKey(user)) {
            return new ValidationResult(Message.SUCCESS, Message.SUCCESS.getErrorMessage());
        }
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
                        "Order books are not initialized (check server startup/config.json)");
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
        if (bots.containsKey(user)) {
            return new ValidationResult(Message.SUCCESS, Message.SUCCESS.getErrorMessage());
        }
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
                        "Order books are not initialized (check server startup/config.json)");
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
        Map<String, Object> response = new HashMap<>();
        response.put("price", price);
        response.put("volumeFilled", volumeFilled);
        response.put("errorCode", error.getErrorCode());
        response.put("errorMessage", error.getErrorMessage());
        return response;
    }

    private OrderData processBid(
            Deque<Order> orders, Map<Integer, Integer> askVolumes, Order aggressor) {
        OrderData orderData = new OrderData();
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
            } else if (order.volume > aggressor.volume) {
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
        OrderData orderData = new OrderData();
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
            } else if (order.volume > aggressor.volume) {
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

    public Map<String, Object> bidLimitOrderHandler(String name, Order order) {
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
            orderData.price /= orderData.volume;
        }
        if (order.volume > 0) {
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
            return createLimitOrderResponse(
                    orderData.price, (int) orderData.volume, Message.SUCCESS, msg, orderID);
        } else {
            order.status = Status.FILLED;
        }
        return createLimitOrderResponse(
                orderData.price, (int) orderData.volume, Message.SUCCESS, msg, 0);
    }

    public long bidLimitOrder(String name, Order order) {
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

    public Map<String, Object> askLimitOrderHandler(String name, Order order) {
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
            orderData.price /= orderData.volume;
        }
        if (order.volume > 0) {
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
            return createLimitOrderResponse(
                    orderData.price, (int) orderData.volume, Message.SUCCESS, msg, orderID);
        } else {
            order.status = Status.FILLED;
        }
        return createLimitOrderResponse(
                orderData.price, (int) orderData.volume, Message.SUCCESS, msg, 0);
    }

    public long askLimitOrder(String name, Order order) {
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
        return orderBooks.get(ticker).bids;
    }

    protected Map<Integer, Deque<Order>> getAsks(String ticker) {
        return orderBooks.get(ticker).asks;
    }

    public List<PriceLevel> getBidPriceLevels(String ticker) {
        Map<Integer, Integer> bidVolumes = orderBooks.get(ticker).bidVolumes;
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : bidVolumes.entrySet()) {
            priceLevels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }
        return priceLevels;
    }

    public List<PriceLevel> getAskPriceLevels(String ticker) {
        Map<Integer, Integer> askVolumes = orderBooks.get(ticker).askVolumes;
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : askVolumes.entrySet()) {
            priceLevels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }
        return priceLevels;
    }

    public Order getOrder(String userId, long orderId) {
        if (!userList.validUser(userId)) return null;
        Map<Long, Order> orders = userOrders.get(userId);
        if (!orders.containsKey(orderId)) return null;
        return orders.get(orderId);
    }

    public boolean removeOrder(String userId, long orderId) {
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

    public void removeAll(String userId) {
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

    public OrderData processMarketOrder(
            Deque<Order> orders, Map<Integer, Integer> volumeMap, Order aggressor, Side side) {
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
            if (side == Side.BID)
                aggressorVolume =
                        Math.min(
                                aggressorVolume,
                                userList.getValidBidVolume(
                                        aggressor.name, order.ticker, order.price));
            else if (side == Side.ASK) {
                aggressorVolume =
                        Math.min(
                                aggressorVolume,
                                userList.getValidAskVolume(aggressor.name, order.ticker));
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
            return createMarketOrderResponse(
                    orderData.price, (int) orderData.volume, Message.PARTIAL_FILL);
        }

        return createMarketOrderResponse(orderData.price, (int) orderData.volume, Message.SUCCESS);
    }

    public int bidMarketOrder(String name, String ticker, int volume) {
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
            return createMarketOrderResponse(
                    orderData.price, (int) orderData.volume, Message.PARTIAL_FILL);
        }

        return createMarketOrderResponse(orderData.price, (int) orderData.volume, Message.SUCCESS);
    }

    public int askMarketOrder(String name, String ticker, int volume) {
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

    public String getUserDetails(String username) {
        JSONObject userListDetails = userList.getUserDetailsAsJson(username, latestPrice);

        // Organize active orders by ticker
        JSONObject ordersByTicker = new JSONObject();

        if (userOrders.containsKey(username)) {
            Map<Long, Order> orders = userOrders.get(username);

            for (Map.Entry<Long, Order> entry : orders.entrySet()) {
                Order order = entry.getValue();

                if (order.status == Status.ACTIVE) {
                    JSONObject orderDetails = new JSONObject();
                    orderDetails.put("orderId", entry.getKey());
                    orderDetails.put("price", order.price);
                    orderDetails.put("volume", order.volume);
                    orderDetails.put("side", order.side.toString());

                    // If ticker key does not exist, initialize it with a new JSONArray
                    if (!ordersByTicker.has(order.ticker)) {
                        ordersByTicker.put(order.ticker, new JSONArray());
                    }

                    // Add the order details to the appropriate ticker array
                    ordersByTicker.getJSONArray(order.ticker).put(orderDetails);
                }
            }
        }

        // Ensure ordersByTicker is added even if empty
        userListDetails.put("Orders", ordersByTicker);
        return userListDetails.toString();
    }

    public void getLeaderboard(TaskFuture<ArrayList<LeaderboardEntry>> future) {
        future.setData(userList.getLeaderboard(latestPrice));
        future.markAsComplete();
    }

    public void executeAuction(String user, int bid) {
        userList.adjustUserBalance(user, -bid);
    }
}
