package HighThroughPutExchange.API.authentication;

import HighThroughPutExchange.API.api_objects.requests.BasePrivateRequest;
import HighThroughPutExchange.API.api_objects.requests.PrivatePageRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RateLimiter {

    private ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> rates;
    private final long MAX_REQUESTS_PER_SECOND = 5;

    public RateLimiter() {
        rates = new ConcurrentHashMap<>();
    }

    public boolean processRequest(BasePrivateRequest req) {
        ConcurrentLinkedDeque<Long> deque = rates.get(req.getUsername());
        if (deque == null) {
            deque = new ConcurrentLinkedDeque<>();
            rates.put(req.getUsername(), deque);
        }
        if (deque.size() < MAX_REQUESTS_PER_SECOND || System.currentTimeMillis() - deque.getLast() > 1000) {
            if (deque.size() == 5) {
                deque.removeLast();
            }
            deque.addFirst(System.currentTimeMillis());
            return true;
        }
        return false;
    }
}
