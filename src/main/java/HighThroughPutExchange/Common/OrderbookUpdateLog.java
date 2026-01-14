package HighThroughPutExchange.Common;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class OrderbookUpdateLog {
    private final ConcurrentHashMap<Long, OrderbookUpdate> log;

    public OrderbookUpdateLog() {
        this.log = new ConcurrentHashMap<>();
    }

    public synchronized void append(OrderbookUpdate update) {
        if (update == null) {
            throw new IllegalArgumentException("update cannot be null");
        }

        log.put(update.getUpdateId(), update);
    }

    public synchronized List<OrderbookUpdate> get(long from) {
        java.util.ArrayList<OrderbookUpdate> out = new java.util.ArrayList<>();

        for (Entry<Long, OrderbookUpdate> entry : log.entrySet()) {
            Long id = entry.getKey();
            if (id != null && id > from) {
                OrderbookUpdate update = entry.getValue();
                if (update != null) {
                    out.add(update);
                }
            }
        }

        out.sort(Comparator.comparingLong(OrderbookUpdate::getUpdateId));
        return out;
    }
}
