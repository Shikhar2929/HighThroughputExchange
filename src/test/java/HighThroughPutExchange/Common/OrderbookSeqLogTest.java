package HighThroughPutExchange.Common;

import static org.junit.jupiter.api.Assertions.*;

import HighThroughPutExchange.MatchingEngine.PriceChange;
import HighThroughPutExchange.MatchingEngine.Side;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderbookSeqLogTest {
    @Test
    void append_null_throws() {
        OrderbookSeqLog log = new OrderbookSeqLog();
        assertThrows(IllegalArgumentException.class, () -> log.append(null));
    }

    @Test
    void getBySeq_returnsInsertedUpdates() {
        OrderbookSeqLog log = new OrderbookSeqLog();

        // Insert out-of-order; lookup is by exact seq.
        log.append(new OrderbookUpdate(5, List.of(new PriceChange("A", 105, 1, Side.BID))));
        log.append(new OrderbookUpdate(2, List.of(new PriceChange("A", 102, 2, Side.ASK))));
        log.append(new OrderbookUpdate(3, List.of(new PriceChange("B", 200, 3, Side.BID))));

        assertEquals(2L, log.getMinSeq());

        assertTrue(log.getBySeq(2).isPresent());
        assertTrue(log.getBySeq(3).isPresent());
        assertTrue(log.getBySeq(5).isPresent());
        assertTrue(log.getBySeq(4).isEmpty());
    }

    @Test
    void append_sameId_overwritesExisting() {
        OrderbookSeqLog log = new OrderbookSeqLog();

        log.append(new OrderbookUpdate(7, List.of(new PriceChange("A", 100, 1, Side.BID))));
        log.append(new OrderbookUpdate(7, List.of(new PriceChange("A", 101, 2, Side.BID))));

        assertTrue(log.getBySeq(7).isPresent());
        OrderbookUpdate got = log.getBySeq(7).get();
        assertEquals(7, got.getSeq());
        assertEquals(101, got.getPriceChanges().get(0).getPrice());
        assertEquals(2, got.getPriceChanges().get(0).getVolume());
    }

    @Test
    void boundedRetention_evictsOldestSeq() {
        OrderbookSeqLog log = new OrderbookSeqLog(2);

        log.append(new OrderbookUpdate(1, List.of()));
        log.append(new OrderbookUpdate(2, List.of()));
        log.append(new OrderbookUpdate(3, List.of()));

        // Only seq 2 and 3 should remain
        assertEquals(2L, log.getMinSeq());

        assertTrue(log.getBySeq(1).isEmpty());
        assertTrue(log.getBySeq(2).isPresent());
        assertTrue(log.getBySeq(3).isPresent());
    }

    @Test
    void nextSeqAndAppend_allocatesSeqAndStoresUpdate() {
        SeqGenerator seqGenerator = new SeqGenerator();
        seqGenerator.reset();

        OrderbookSeqLog log = new OrderbookSeqLog(10);
        long seq =
                log.nextSeqAndAppend(seqGenerator, List.of(new PriceChange("A", 101, 1, Side.BID)));

        assertEquals(0L, seq);
        assertTrue(log.getBySeq(0L).isPresent());
        OrderbookUpdate got = log.getBySeq(0L).get();
        assertEquals(0L, got.getSeq());
        assertEquals(1, got.getPriceChanges().size());
    }
}
