package HighThroughPutExchange.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.springframework.stereotype.Component;

@Component
public class OrderbookSeqLog {
    private static final int DEFAULT_MAX_ENTRIES = 10_000;

    private final int maxEntries;
    private final ConcurrentSkipListMap<Long, OrderbookUpdate> log;

    public OrderbookSeqLog() {
        this(DEFAULT_MAX_ENTRIES);
    }

    public OrderbookSeqLog(int maxEntries) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be > 0");
        }

        this.maxEntries = maxEntries;
        this.log = new ConcurrentSkipListMap<>();
    }

    public synchronized void append(OrderbookUpdate update) {
        if (update == null) {
            throw new IllegalArgumentException("update cannot be null");
        }

        log.put(update.getSeq(), update);

        // Keep only the latest N updates (by seq).
        while (log.size() > maxEntries) {
            log.pollFirstEntry();
        }
    }

    public synchronized List<OrderbookUpdate> get(long from) {
        ArrayList<OrderbookUpdate> out = new ArrayList<>();
        NavigableMap<Long, OrderbookUpdate> tail = log.tailMap(from, false);

        for (Entry<Long, OrderbookUpdate> entry : tail.entrySet()) {
            OrderbookUpdate update = entry.getValue();
            if (update != null) {
                out.add(update);
            }
        }

        return out;
    }

    public synchronized Long getMinSeq() {
        return log.isEmpty() ? null : log.firstKey();
    }

    public int getMaxEntries() {
        return maxEntries;
    }
}
