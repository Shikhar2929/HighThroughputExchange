package HighThroughPutExchange.MatchingEngine;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.Iterator;

import HighThroughPutExchange.Common.ChartTrackerSingleton;
import HighThroughPutExchange.Common.Message;
import HighThroughPutExchange.Common.TaskFuture;
import com.fasterxml.jackson.databind.ObjectMapper;

// todo: get rid of usage of JSON library
 import org.json.JSONArray;
 import org.json.JSONObject;

public class MatchingEngine {
    private Map<String, OrderBook> orderBooks = new HashMap<>();
    private Map<String, Map<Long, Order>> userOrders = new HashMap<>(); // UserName, OrderId, Order
    private Map<String, Double> latestPrice = new HashMap<>(); // For PnL
    private Map<String, Double> bots = new HashMap<>();
    private ChartTrackerSingleton chartTrackerSingleton = ChartTrackerSingleton.getInstance();
    private UserList userList = new UserList();
    private long orderID = 0;
    public MatchingEngine() {
        userList.setInfinite(false);
    }
    public MatchingEngine(boolean initialize){
        if (initialize)
            initializeGameMode();
    }
    public MatchingEngine(double positionLimit) {
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
            e.printStackTrace();
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
            if (mode.equals("finite"))
                userList.setInfinite(false);
            else {
                userList.setInfinite(true);
                JSONObject defaults = configData.getJSONObject("defaults");
                double positionLimit = defaults.getDouble("positionLimit");
                userList.setPositionLimit(positionLimit);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean initializeBot(String username) {
        bots.put(username, 0.0);
        initializeUserBalance(username, 0.0);
        for (String ticker : orderBooks.keySet()) {
            initializeUserTickerVolume(username, ticker, 0.0);
        }
        return true;
    }

    public boolean initializeUserBalance(String username, double balance) {
        System.out.println("Initializing: " + username);
        return userList.initializeUser(username, balance);
    }
    public boolean initializeUserTickerVolume(String username, String ticker, double volume) {
        System.out.println("Initializing: " + username + " with ticker: " + ticker);
        return userList.initializeUserQuantity(username, ticker, volume);
    }
    public double getUserBalance(String username) {
        return userList.getUserBalance(username);
    }
    public double getPnl(String username) {
        return userList.getUnrealizedPnl(username, latestPrice);
    }
    public ArrayList getRecentTrades() {
        ArrayList<PriceChange> recentTrades = RecentTrades.getRecentTrades();
        //for (Trade trade : recentTrades)
        ///    System.out.println(trade);
        return recentTrades;
    }
    public double getTickerBalance(String username, String ticker) {
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

            for (String ticker: configData.getDefaults().getTickers()) {
                System.out.println("Ticker: " + ticker);
                initializeTicker(ticker);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public boolean initializeAllTickers() {
        try {
            // Read the JSON file
            System.out.println("Current Working Directory: " + Paths.get("").toAbsolutePath());
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
                System.out.println("Ticker: " + ticker);
                initializeTicker(ticker);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

            System.out.println("Default Balance: " + configData.getDefaults().getDefaultBalance());
            for (String key: configData.getDefaults().getBalances().keySet()) {
                System.out.println("Ticker: " + key + ", Balance: " + configData.getDefaults().getBalances().get(key));
                initializeUserTickerVolume(user, key, configData.getDefaults().getBalances().get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            String mode = configData.getString("mode");
            if (!userList.getMode()) {
                // Extract and process balances
                JSONObject defaults = configData.getJSONObject("defaults");
                double defaultBalance = defaults.getDouble("defaultBalance");
                JSONObject balances = defaults.getJSONObject("balances");
                System.out.println("Default Balance: " + defaultBalance);
                initializeUserBalance(user, defaultBalance);
                Iterator<String> keys = balances.keys();
                while (keys.hasNext()) {
                    String ticker = keys.next();
                    double balance = balances.getDouble(ticker);
                    System.out.println("Ticker: " + ticker + ", Balance: " + balance);
                    initializeUserTickerVolume(user, ticker, balance);
                }
            }
            else {
                JSONObject defaults = configData.getJSONObject("defaults");
                JSONObject balances = defaults.getJSONObject("balances");
                Iterator<String> keys = balances.keys();
                System.out.println("Infinite Mode");
                userList.initializeUser(user);
                while (keys.hasNext()) {
                    String ticker = keys.next();
                    double balance = balances.getDouble(ticker);
                    System.out.println("Ticker: " + ticker + ", Balance: " + balance);
                    initializeUserTickerVolume(user, ticker, balance);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public double getPrice(String ticker) {
        return latestPrice.getOrDefault(ticker,0.0);
    }
    public void setPrice(String ticker, double price) {
        latestPrice.put(ticker, price);
        chartTrackerSingleton.updatePrice(ticker, price);
    }
    public void setPriceClearOrderBook(Map<String, Double> updatedPrices, TaskFuture<String> future) {
        for (String ticker : updatedPrices.keySet()) {
            if (!orderBooks.containsKey(ticker)) {
                future.setData("BAD TICKER");
            }
        }
        for (Map.Entry<String, Double> entry : updatedPrices.entrySet()) {
            setPrice(entry.getKey(), entry.getValue());
            zeroVolume(orderBooks.get(entry.getKey()), entry.getKey());
        }
        for (String user : userOrders.keySet()) {
            userOrders.get(user).clear();
        }
        future.setData("SUCCESS ALL CLEARED");
    }

    public double getHighestBid(String ticker) {
        if (!orderBooks.containsKey(ticker)) return 0.0;
        TreeMap<Double, Deque<Order>> bids = orderBooks.get(ticker).bids;
        if (bids.isEmpty()) {
            return 0.0;
        }
        return bids.lastKey();
    }
    public double getLowestAsk(String ticker) {
        if (!orderBooks.containsKey(ticker)) return 0.0;
        TreeMap<Double, Deque<Order>> asks = orderBooks.get(ticker).asks;
        if (asks.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        return asks.firstKey();
    }
    protected void updateVolume(Map<Double, Double> volumeMap, double price, double delta, String ticker, Side side) {
        volumeMap.put(price, volumeMap.getOrDefault(price, 0.0) + delta);
        if (volumeMap.get(price) <= 0) {
            volumeMap.remove(price);
        }
        double newQuantity = volumeMap.getOrDefault(price, 0.0);
        //System.out.printf("Price: %f Quantity: %f\n", price, newQuantity);
        RecentTrades.addTrade(ticker, price, newQuantity, side);
    }
    private void zeroVolume(OrderBook orderBook, String ticker) {
        for (double price : orderBook.bidVolumes.keySet())
            RecentTrades.addTrade(ticker, price, 0.0, Side.BID);
        for (double price : orderBook.askVolumes.keySet())
            RecentTrades.addTrade(ticker, price, 0.0, Side.ASK);
        orderBook.clearOrderBook();
    }
    private boolean validateBidOrder(String user, Order order) {
        if (bots.containsKey(user)) {
            return true;
        }
        if (order.volume <= 0.0 || order.price <= 0.0 || order.status != Status.ACTIVE) {
            System.out.println("Bad Bid Parameters");
            return false;
        }
        if (!orderBooks.containsKey(order.ticker)) {
            System.out.println("Bad Ticker");
            return false;
        }
        if (!userList.validUser(user)) {
            System.out.println("Bad User");
            return false;
        }
        if (!userList.validBidParameters(user, order)) {
            System.out.println("Invalid Volume Parameters");
            return false;
        }
        return true;
    }
    private boolean validateAskOrder(String user, Order order) {
        if (bots.containsKey(user)) {
            return true;
        }
        if (order.volume <= 0.0 || order.price <= 0.0 || order.status != Status.ACTIVE) {
            System.out.println("Bad Ask Parameters");
            return false;
        }
        if (!orderBooks.containsKey(order.ticker)) {
            System.out.println("Bad Ticker");
            return false;
        }
        if (!userList.validUser(user)) {
            System.out.println("Bad User");
            return false;
        }
        if (!userList.validAskQuantity(user, order.ticker, order.volume)) {
            System.out.println("Insufficient Sell Funds");
            return false;
        }
        return true;
    }
    private Map<String, Object> createLimitOrderResponse(double price, double volumeFilled, Message error, long orderId) {
        Map<String, Object> response = new HashMap<>();
        response.put("price", price);
        response.put("volumeFilled", volumeFilled);
        response.put("errorCode", error.getErrorCode());
        response.put("errorMessage", error.getErrorMessage());
        response.put("orderId", orderId);
        return response;
    }
    private Map<String, Object> createMarketOrderResponse(double price, double volumeFilled, Message error) {
        Map<String, Object> response = new HashMap<>();
        response.put("price", price);
        response.put("volumeFilled", volumeFilled);
        response.put("errorCode", error.getErrorCode());
        response.put("errorMessage", error.getErrorMessage());
        return response;
    }
    private OrderData processBid(Deque<Order> orders, Map<Double, Double> askVolumes, Order aggressor) {
        OrderData orderData = new OrderData();
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
            }
            else if (order.volume > aggressor.volume) {
                double volumeTraded = aggressor.volume;
                updateVolume(askVolumes, order.price, -volumeTraded, order.ticker, Side.ASK);
                userList.adjustUserAskBalance(order.name, order.ticker, -volumeTraded);

                userList.adjustUserBalance(order.name, volumeTraded * order.price);
                userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);

                userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                userList.adjustUserTickerBalance(order.name, order.ticker, -volumeTraded);
                //Update OrderBook to reflect new price of the ticker
                setPrice(order.ticker, order.price);
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
                orderData.linearCombination(order.price, volumeTraded);
            } else {
                double volumeTraded = order.volume;
                updateVolume(askVolumes, order.price, -volumeTraded, order.ticker, Side.ASK);
                userList.adjustUserAskBalance(order.name, order.ticker, -volumeTraded);

                userList.adjustUserBalance(order.name, volumeTraded * order.price);
                userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);

                userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                userList.adjustUserTickerBalance(order.name, order.ticker, -volumeTraded);
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
    public OrderData processAsk(Deque<Order> orders,  Map<Double, Double> bidVolumes, Order aggressor) {
        OrderData orderData = new OrderData();
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
            }
            else if (order.volume > aggressor.volume) {
                double volumeTraded = aggressor.volume;
                updateVolume(bidVolumes, order.price, -volumeTraded, order.ticker, Side.BID);
                userList.adjustUserBidBalance(order.name, order.ticker, -volumeTraded);

                userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                userList.adjustUserBalance(order.name, -volumeTraded * order.price);

                userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);
                setPrice(order.ticker, order.price);
                //RecentTrades.addTrade(order.name, aggressor.name, order.ticker, order.price, volumeTraded);
                order.volume = order.volume - aggressor.volume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
                orderData.linearCombination(order.price, volumeTraded);
            } else {
                double volumeTraded = order.volume;
                updateVolume(bidVolumes, order.price, -volumeTraded, order.ticker, Side.BID);
                userList.adjustUserBidBalance(order.name, order.ticker, -volumeTraded);

                userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                userList.adjustUserBalance(order.name, -volumeTraded * order.price);

                userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);
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
        if (!validateBidOrder(name, order)) {
            System.out.println("BAD PARAMETERS");
            return createLimitOrderResponse(0.0, 0.0, Message.BAD_INPUT, -1);
        }
        TreeMap<Double, Deque<Order>> asks = orderBooks.get(order.ticker).asks;
        Map<Double, Double> askVolumes = orderBooks.get(order.ticker).askVolumes;
        TreeMap<Double, Deque<Order>> bids = orderBooks.get(order.ticker).bids;
        Map<Double, Double> bidVolumes = orderBooks.get(order.ticker).bidVolumes;
        OrderData orderData = new OrderData();
        //validate order ensures that there is sufficient balance
        while (order.volume > 0 && !asks.isEmpty() && asks.firstKey() <= order.price) {
            Deque<Order> orderList = asks.get(asks.firstKey());
            orderData.add(processBid(orderList, askVolumes, order));
            if (orderList.isEmpty()) {
                asks.pollFirstEntry();
            }
        }
        System.out.printf("BID LIMIT ORDER Remaining Volume to be placed on the orderbook: %.2f\n", order.volume);
        String msg = String.format("SUCCESS! BID LIMIT ORDER Remaining Volume to be placed on the orderbook: %.2f\n", order.volume);
        Message e = Message.SUCCESS; e.setErrorMessage(msg);
        if (orderData.volume > 0) {
            orderData.price /= orderData.volume;
        }
        if (order.volume > 0) {
            order.status = Status.ACTIVE;
            bids.computeIfAbsent(order.price, k -> new LinkedList<>()).add(order);
            updateVolume(bidVolumes, order.price, order.volume, order.ticker, Side.BID);
            //userList.adjustUserBalance(name, -order.price * order.volume);
            userList.adjustUserBidBalance(name, order.ticker, order.volume);
            orderID++;
            if (userOrders.containsKey(order.name)) {
                userOrders.get(order.name).put(orderID, order);
            }
            else {
                userOrders.put(order.name, new HashMap<>());
                userOrders.get(order.name).put(orderID, order);
            }
            return createLimitOrderResponse(orderData.price, orderData.volume, e, orderID);
        }
        else {
            order.status = Status.FILLED;
        }
        return createLimitOrderResponse(orderData.price, orderData.volume, e, 0);
    }

    public long bidLimitOrder(String name, Order order) {
        return (long) bidLimitOrderHandler(name, order).get("orderId");
    }
    public void bidLimitOrder(String name, Order order, TaskFuture<String> future) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(bidLimitOrderHandler(name, order));
            future.setData(jsonResponse);
        }
        catch (Exception e) {
            System.out.println("Bad JSON, Error in Bid Limit Order Handler");
        }
    }
    public Map<String, Object> askLimitOrderHandler(String name, Order order) {
        if (!validateAskOrder(name, order)) {
            return createLimitOrderResponse(0.0, 0.0, Message.BAD_INPUT, -1);
        }
        TreeMap<Double, Deque<Order>> asks = orderBooks.get(order.ticker).asks;
        Map<Double, Double> askVolumes = orderBooks.get(order.ticker).askVolumes;
        TreeMap<Double, Deque<Order>> bids = orderBooks.get(order.ticker).bids;
        Map<Double, Double> bidVolumes = orderBooks.get(order.ticker).bidVolumes;
        OrderData orderData = new OrderData();
        while (order.volume > 0 && !bids.isEmpty() && bids.lastKey() >= order.price) {
            Deque<Order> orderList = bids.get(bids.lastKey());
            orderData.add(processAsk(orderList, bidVolumes, order));
            if (orderList.isEmpty()) {
                bids.pollLastEntry();
            }
        }
        System.out.printf("ASK LIMIT ORDER Remaining Volume to be placed on the orderbook: %.2f\n", order.volume);
        String msg = String.format("SUCCESS! ASK LIMIT ORDER Remaining Volume to be placed on the orderbook: %.2f\n", order.volume);
        Message e = Message.SUCCESS; e.setErrorMessage(msg);
        if (orderData.volume > 0) {
            System.out.printf("Ask OrderData.price %f\n", orderData.price);
            orderData.price /= orderData.volume;
        }
        if (order.volume > 0) {
            order.status = Status.ACTIVE;
            asks.computeIfAbsent(order.price, k -> new LinkedList<>()).add(order);
            orderID++;
            updateVolume(askVolumes, order.price, order.volume, order.ticker, Side.ASK);
            //userList.adjustUserTickerBalance(order.name, order.ticker, -order.volume);
            userList.adjustUserAskBalance(order.name, order.ticker, order.volume);
            if (userOrders.containsKey(order.name)) {
                userOrders.get(order.name).put(orderID, order);
            }
            else {
                userOrders.put(order.name, new HashMap<>());
                userOrders.get(order.name).put(orderID, order);
            }
            return createLimitOrderResponse(orderData.price, orderData.volume, e, orderID);
        }
        else {
            order.status = Status.FILLED;
        }
        return createLimitOrderResponse(orderData.price, orderData.volume, e, 0);
    }


    public long askLimitOrder(String name, Order order) {
        return (long) askLimitOrderHandler(name, order).get("orderId");
    }
    public void askLimitOrder(String name, Order order, TaskFuture<String> future) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(askLimitOrderHandler(name, order));
            future.setData(jsonResponse);
        }
        catch (Exception e) {
            System.out.println("Bad JSON, Error in Ask Limit Order");
        }
    }
    protected Map<Double, Deque<Order>> getBids(String ticker) {
        return orderBooks.get(ticker).bids;
    }
    protected Map<Double, Deque<Order>> getAsks(String ticker) {
        return orderBooks.get(ticker).asks;
    }
    public List<PriceLevel> getBidPriceLevels(String ticker) {
        Map<Double, Double> bidVolumes = orderBooks.get(ticker).bidVolumes;
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (Map.Entry<Double, Double> entry : bidVolumes.entrySet()) {
            priceLevels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }
        return priceLevels;
    }
    public List<PriceLevel> getAskPriceLevels(String ticker) {
        Map<Double, Double> askVolumes = orderBooks.get(ticker).askVolumes;
        List<PriceLevel> priceLevels = new ArrayList<>();
        for (Map.Entry<Double, Double> entry : askVolumes.entrySet()) {
            priceLevels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }
        return priceLevels;
    }

    /*public void display() {
        System.out.println("BID ---- ");
        for (Map.Entry<Double, Deque<Order>> entry : bids.entrySet()) {
            for (Order bid : entry.getValue()) {
                System.out.println(bid.name + " " + bid.price + " " + bid.volume);
            }
        }
    }*/
    public Order getOrder(String userId, long orderId) {
        if (!userList.validUser(userId))
            return null;
        Map<Long, Order> orders = userOrders.get(userId);
        if (!orders.containsKey(orderId))
            return null;
        return orders.get(orderId);
    }
    public boolean removeOrder(String userId, long orderId) {
        if (!userList.validUser(userId))
            return false;
        Map<Long, Order> orders = userOrders.get(userId);
        if (orders != null) {
            if (orders.containsKey(orderId) && orders.get(orderId).status == Status.ACTIVE) {
                Order order = orders.get(orderId);
                orders.get(orderId).status = Status.CANCELLED;
                if (order.side == Side.BID) {
                    //userList.adjustUserBalance(userId, order.price * order.volume);
                    userList.adjustUserBidBalance(userId, order.ticker, -order.volume);
                    Map<Double, Double> bidVolumes = orderBooks.get(order.ticker).bidVolumes;
                    updateVolume(bidVolumes, order.price, -order.volume, order.ticker, Side.BID);
                }
                else {
                    //userList.adjustUserTickerBalance(userId, order.ticker, order.volume);
                    userList.adjustUserAskBalance(userId, order.ticker, -order.volume);
                    Map<Double, Double> askVolumes = orderBooks.get(order.ticker).askVolumes;
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
            future.setData(Message.AUTHENTICATION_FAILED.toString());
            return false;
        }
        Map<Long, Order> orders = userOrders.get(userId);
        if (orders != null) {
            if (orders.containsKey(orderId) && orders.get(orderId).status == Status.ACTIVE) {
                Order order = orders.get(orderId);
                orders.get(orderId).status = Status.CANCELLED;
                if (order.side == Side.BID) {
                    //userList.adjustUserBalance(userId, order.price * order.volume);
                    userList.adjustUserBidBalance(userId, order.ticker, -order.volume);
                    Map<Double, Double> bidVolumes = orderBooks.get(order.ticker).bidVolumes;
                    updateVolume(bidVolumes, order.price, -order.volume, order.ticker, Side.BID);
                }
                else {
                    // userList.adjustUserTickerBalance(userId, order.ticker, order.volume);
                    userList.adjustUserAskBalance(userId, order.ticker, -order.volume);
                    Map<Double, Double> askVolumes = orderBooks.get(order.ticker).askVolumes;
                    updateVolume(askVolumes, order.price, -order.volume, order.ticker, Side.ASK);
                }
                future.setData(String.format("{\"errorCode\": %d, \"errorMessage\": \"%s\"}", Message.SUCCESS.getErrorCode(), String.format("removed order with properties - id: %d, volume: %f, ", orderId, orders.remove(orderId).volume)));
                return true;
            }
        }
        future.setData(Message.BAD_INPUT.toString());
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

        boolean allRemoved = true;

        // Iterate through all orders and remove each
        for (Long orderId : new ArrayList<>(orders.keySet())) {
            boolean removed = removeOrder(userId, orderId);
        }
    }
    public void removeAll(String userId, TaskFuture<String> future) {
        if (!userList.validUser(userId)) {
            return;
        }

        // Retrieve user's orders
        Map<Long, Order> orders = userOrders.get(userId);
        if (orders == null || orders.isEmpty()) {
            return; // No orders to remove
        }

        boolean allRemoved = true;

        double volumeRemoved = 0;

        // Iterate through all orders and remove each
        for (Long orderId : new ArrayList<>(orders.keySet())) {
            double vol = orders.get(orderId).volume;
            boolean removed = removeOrder(userId, orderId);
            if (removed) {
                volumeRemoved += vol;
            }
        }

        future.setData(String.format("{\"errorCode\": %d, \"errorMessage\": \"%s\"}", Message.SUCCESS.getErrorCode(), String.format("Removed total volume of %f", volumeRemoved)));
    }
    public OrderData processMarketOrder(Deque<Order> orders, Map<Double, Double> volumeMap, Order aggressor, Side side) {
        double overallVolume = 0.0;
        OrderData orderData = new OrderData();
        while (aggressor.volume > 0 && !orders.isEmpty()) {
            Order order = orders.peek();
            if (order.status == Status.CANCELLED) {
                orders.poll();
                continue;
            }
            // 2 Cases:
            // First Case: Finite Stack - only sell what you can own
            double aggressorVolume = aggressor.volume;
            if (side == Side.BID)
                aggressorVolume = Math.min(aggressorVolume, userList.getValidBidVolume(aggressor.name, order.ticker, order.price));
            else if (side == Side.ASK) {
                aggressorVolume = Math.min(aggressorVolume, userList.getValidAskVolume(aggressor.name, order.ticker));
            }
            if (order.volume > aggressorVolume) {
                double volumeTraded = aggressorVolume;
                double tradePrice = order.price;
                if (side == Side.BID) {
                    userList.adjustUserAskBalance(order.name, order.ticker, -volumeTraded);

                    userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, volumeTraded * order.price);
                    //Add volume to the aggressor's ticker balance, since it is buying
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                    userList.adjustUserTickerBalance(order.name, order.ticker, -volumeTraded);
                    //Update the ask volume map and the ask if it is a bid order
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.ASK);
                }
                else {
                    userList.adjustUserBidBalance(order.name, order.ticker, -volumeTraded);

                    userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, -volumeTraded * order.price);
                    //Remove volume from the aggressor's ticker balance and add to the order's balance
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                    userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);
                    //Update the bid volume map and the bid if it is a bid order
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.BID);
                    }
                setPrice(order.ticker, tradePrice);
                order.volume -= aggressorVolume;
                orderData.linearCombination(tradePrice, volumeTraded);
                overallVolume += aggressorVolume;
                aggressor.volume = 0;
                aggressor.status = Status.FILLED;
            } else {
                double volumeTraded = order.volume;
                double tradePrice = order.price;
                if (side == Side.BID) {
                    userList.adjustUserAskBalance(order.name, order.ticker, -volumeTraded);

                    userList.adjustUserBalance(aggressor.name, -volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, volumeTraded * order.price);
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, volumeTraded);
                    userList.adjustUserTickerBalance(order.name, order.ticker, -volumeTraded);
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.ASK);
                }
                else {
                    userList.adjustUserBidBalance(order.name, order.ticker, -volumeTraded);

                    userList.adjustUserBalance(aggressor.name, volumeTraded * order.price);
                    userList.adjustUserBalance(order.name, -volumeTraded * order.price);
                    userList.adjustUserTickerBalance(aggressor.name, order.ticker, -volumeTraded);
                    userList.adjustUserTickerBalance(order.name, order.ticker, volumeTraded);
                    updateVolume(volumeMap, tradePrice, -volumeTraded, order.ticker, Side.BID);
                }
                setPrice(order.ticker, tradePrice);
                orderData.linearCombination(tradePrice, volumeTraded);
                overallVolume += order.volume;
                aggressor.volume -= order.volume;
                order.volume = 0;
                order.status = Status.FILLED;
                orders.poll();
            }
        }
        return orderData;
    }
    public Map<String, Object> bidMarketOrderHandler(String name, String ticker, double volume) {
        if (!userList.validUser(name) && !bots.containsKey(name)) {
            System.out.println("Invalid");
            return createMarketOrderResponse(0.0, 0.0, Message.AUTHENTICATION_FAILED);
        }
        OrderData orderData = new OrderData();
        Order marketOrder = new Order(name, ticker, 0, volume, Side.BID, Status.ACTIVE); // Price is 0 for market orders
        double volumeFilled = 0.0;
        TreeMap<Double, Deque<Order>> asks = orderBooks.get(ticker).asks;
        Map<Double, Double> askVolumes = orderBooks.get(ticker).askVolumes;
        while (marketOrder.volume > 0 && !asks.isEmpty()) {
            Deque<Order> orderList = asks.get(asks.firstKey());
            orderData.add(processMarketOrder(orderList, askVolumes, marketOrder, Side.BID));
            if (orderList.isEmpty()) {
                asks.pollFirstEntry();
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
        return createMarketOrderResponse(orderData.price, orderData.volume, Message.SUCCESS);
    }


    public double bidMarketOrder(String name, String ticker, double volume) {
        return (double) bidMarketOrderHandler(name, ticker, volume).get("volumeFilled");
    }

    public void bidMarketOrder(String name, String ticker, double volume, TaskFuture<String> future) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(bidMarketOrderHandler(name, ticker, volume));
            future.setData(jsonResponse);
        }
        catch (Exception e) {
            System.out.println("Bad JSON, Error in Bid Market Order Handler");
        }
    }
    public Map<String, Object> askMarketOrderHandler(String name, String ticker, double volume) {
        if (!userList.validUser(name) && !bots.containsKey(name)) {
            return createMarketOrderResponse(0.0, 0.0, Message.AUTHENTICATION_FAILED);
        }
        if (!userList.validAskQuantity(name, ticker, volume) && !bots.containsKey(name)) {
            return createMarketOrderResponse(0.0, 0.0, Message.BAD_INPUT);
        }
        OrderData orderData = new OrderData();
        Order marketOrder = new Order(name, ticker, 0, volume, Side.ASK, Status.ACTIVE); // Price is 0 for market orders
        double volumeFilled = 0.0;
        TreeMap<Double, Deque<Order>> bids = orderBooks.get(ticker).bids;
        Map<Double, Double> bidVolumes = orderBooks.get(ticker).bidVolumes;
        while (marketOrder.volume > 0 && !bids.isEmpty()) {
            Deque<Order> orderList = bids.get(bids.lastKey());
            orderData.add(processMarketOrder(orderList, bidVolumes, marketOrder, Side.ASK));
            if (orderList.isEmpty()) {
                bids.pollLastEntry();
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
        return createMarketOrderResponse(orderData.price, orderData.volume, Message.SUCCESS);
    }
    public double askMarketOrder(String name, String ticker, double volume) {
        return (double) askMarketOrderHandler(name, ticker, volume).get("volumeFilled");
    }
    public void askMarketOrder(String name, String ticker, double volume, TaskFuture<String> future) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(askMarketOrderHandler(name, ticker, volume));
            future.setData(jsonResponse);
        }
        catch (Exception e) {
            System.out.println("Bad JSON, Error in Bid Market Order Handler");
        }
    }
    public String getUserDetails(String username) {
        ObjectMapper objectMapper = new ObjectMapper();
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
    public void executeAuction(String user, double bid) {
        userList.adjustUserBalance(user, -bid);
    }
}
