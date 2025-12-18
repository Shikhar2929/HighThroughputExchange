package HighThroughPutExchange.API.service;

import HighThroughPutExchange.Common.TaskFuture;
import HighThroughPutExchange.Common.TaskQueue;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import HighThroughPutExchange.MatchingEngine.Order;
import HighThroughPutExchange.MatchingEngine.Side;
import HighThroughPutExchange.MatchingEngine.Status;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final MatchingEngine matchingEngine;

    public OrderService(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    public String placeLimitOrder(String username, String ticker, int price, int volume, boolean bid) {
        TaskFuture<String> future = new TaskFuture<>();
        TaskQueue.addTask(() -> {
            Order order = new Order(username, ticker, price, volume, bid ? Side.BID : Side.ASK, Status.ACTIVE);
            if (bid) {
                matchingEngine.bidLimitOrder(username, order, future);
            } else {
                matchingEngine.askLimitOrder(username, order, future);
            }
            future.markAsComplete();
        });
        future.waitForCompletion();
        return future.getData();
    }

    public String placeMarketOrder(String username, String ticker, int volume, boolean bid) {
        TaskFuture<String> future = new TaskFuture<>();
        TaskQueue.addTask(() -> {
            if (bid) {
                matchingEngine.bidMarketOrder(username, ticker, volume, future);
            } else {
                matchingEngine.askMarketOrder(username, ticker, volume, future);
            }
            future.markAsComplete();
        });
        future.waitForCompletion();
        return future.getData();
    }

    public String removeOrder(String username, long orderId) {
        TaskFuture<String> future = new TaskFuture<>();
        TaskQueue.addTask(() -> {
            matchingEngine.removeOrder(username, orderId, future);
            future.markAsComplete();
        });
        future.waitForCompletion();
        return future.getData();
    }

    public String removeAll(String username) {
        TaskFuture<String> future = new TaskFuture<>();
        TaskQueue.addTask(() -> {
            matchingEngine.removeAll(username, future);
            future.markAsComplete();
        });
        future.waitForCompletion();
        return future.getData();
    }
}
