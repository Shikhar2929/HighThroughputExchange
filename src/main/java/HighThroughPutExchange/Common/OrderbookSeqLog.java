package HighThroughPutExchange.Common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class OrderbookSeqLog {
    private final ConcurrentHashMap<Long, OrderbookUpdate> log;

    public OrderbookSeqLog() {
        this.log = new ConcurrentHashMap<>();
    }

    public synchronized void append(OrderbookUpdate update) {
        if (update == null) {
            throw new IllegalArgumentException("update cannot be null");
        }

        log.put(update.getSeq(), update);
    }

    public synchronized List<OrderbookUpdate> get(long from) {
        ArrayList<OrderbookUpdate> out = new ArrayList<>();

        for (Entry<Long, OrderbookUpdate> entry : log.entrySet()) {
            Long id = entry.getKey();
            if (id != null && id > from) {
                OrderbookUpdate update = entry.getValue();
                if (update != null) {
                    out.add(update);
                }
            }
        }

        out.sort(Comparator.comparingLong(OrderbookUpdate::getSeq));
        return out;
    }
}
