package hte.api.service;

import hte.api.dtos.operations.LimitOrderOperation;
import hte.api.dtos.operations.MarketOrderOperation;
import hte.api.dtos.operations.Operation;
import hte.api.dtos.responses.OperationResponse;
import hte.common.TaskFuture;
import hte.common.TaskQueue;
import hte.matchingengine.MatchingEngine;
import hte.matchingengine.Order;
import hte.matchingengine.Side;
import hte.matchingengine.Status;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

    private static final int MAX_OPERATIONS = 20;
    private final MatchingEngine matchingEngine;

    public BatchService(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    public int getMaxOperations() {
        return MAX_OPERATIONS;
    }

    public List<OperationResponse> processBatch(String username, List<Operation> operations) {
        List<Runnable> temporaryTaskQueue = new ArrayList<>();
        List<OperationResponse> responses = new ArrayList<>();
        List<TaskFuture<String>> futures = new ArrayList<>();
        boolean success = true;

        for (Operation operation : operations) {
            TaskFuture<String> future = new TaskFuture<>();
            futures.add(future);
            responses.add(new OperationResponse(operation.getType(), null));
            switch (operation.getType()) {
                case "limit_order":
                    {
                        LimitOrderOperation limitOrderOperation = (LimitOrderOperation) operation;
                        temporaryTaskQueue.add(
                                () -> {
                                    Order order =
                                            new Order(
                                                    username,
                                                    limitOrderOperation.getTicker(),
                                                    limitOrderOperation.getPrice(),
                                                    limitOrderOperation.getVolume(),
                                                    limitOrderOperation.getBid()
                                                            ? Side.BID
                                                            : Side.ASK,
                                                    Status.ACTIVE);
                                    if (limitOrderOperation.getBid())
                                        matchingEngine.bidLimitOrder(username, order, future);
                                    else matchingEngine.askLimitOrder(username, order, future);
                                    future.markAsComplete();
                                });
                        break;
                    }
                case "market_order":
                    {
                        MarketOrderOperation marketOrderOperation =
                                (MarketOrderOperation) operation;
                        temporaryTaskQueue.add(
                                () -> {
                                    if (marketOrderOperation.getBid()) {
                                        matchingEngine.bidMarketOrder(
                                                username,
                                                marketOrderOperation.getTicker(),
                                                marketOrderOperation.getVolume(),
                                                future);
                                    } else
                                        matchingEngine.askMarketOrder(
                                                username,
                                                marketOrderOperation.getTicker(),
                                                marketOrderOperation.getVolume(),
                                                future);
                                    future.markAsComplete();
                                });
                        break;
                    }
                case "remove":
                    {
                        hte.api.dtos.operations.RemoveOperation removeOperation =
                                (hte.api.dtos.operations.RemoveOperation) operation;
                        temporaryTaskQueue.add(
                                () -> {
                                    matchingEngine.removeOrder(
                                            username, removeOperation.getOrderId(), future);
                                    future.markAsComplete();
                                });
                        break;
                    }
                case "remove_all":
                    {
                        temporaryTaskQueue.add(
                                () -> {
                                    matchingEngine.removeAll(username, future);
                                    future.markAsComplete();
                                });
                        break;
                    }
                default:
                    {
                        success = false;
                        break;
                    }
            }
        }

        if (!success) {
            return null;
        }

        for (Runnable task : temporaryTaskQueue) {
            TaskQueue.addTask(task);
        }

        for (int i = 0; i < futures.size(); i++) {
            TaskFuture<String> future = futures.get(i);
            future.waitForCompletion();
            OperationResponse response = responses.get(i);
            response.setMessage(future.getData());
        }

        return responses;
    }
}
