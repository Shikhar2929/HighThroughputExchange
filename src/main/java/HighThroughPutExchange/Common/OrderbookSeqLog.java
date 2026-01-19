package HighThroughPutExchange.Common;

import HighThroughPutExchange.MatchingEngine.PriceChange;
import java.util.List;
import java.util.Optional;
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

    // Allocates the next sequence number and appends the update to this log in a
    // single synchronized block.
    public synchronized long nextSeqAndAppend(
            SeqGenerator seqGenerator, List<PriceChange> priceChanges) {
        if (seqGenerator == null) {
            throw new IllegalArgumentException("seqGenerator cannot be null");
        }

        if (priceChanges == null) {
            throw new IllegalArgumentException("priceChanges cannot be null");
        }

        long seq = seqGenerator.getAndIncrement();
        append(new OrderbookUpdate(seq, priceChanges));
        return seq;
    }

    // Returns the update with the given seq if it exists in the retention window.
    public synchronized Optional<OrderbookUpdate> getBySeq(long seq) {
        return Optional.ofNullable(log.get(seq));
    }

    public synchronized Long getMinSeq() {
        return log.isEmpty() ? null : log.firstKey();
    }

    public int getMaxEntries() {
        return maxEntries;
    }
}
