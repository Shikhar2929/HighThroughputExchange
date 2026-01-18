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
    void get_filtersByFromExclusive_andSortsBySeq() {
        OrderbookSeqLog log = new OrderbookSeqLog();

        // Insert out-of-order to ensure get() sorting is tested.
        log.append(new OrderbookUpdate(5, List.of(new PriceChange("A", 105, 1, Side.BID))));
        log.append(new OrderbookUpdate(2, List.of(new PriceChange("A", 102, 2, Side.ASK))));
        log.append(new OrderbookUpdate(3, List.of(new PriceChange("B", 200, 3, Side.BID))));

        List<OrderbookUpdate> all = log.get(-1);
        assertEquals(3, all.size());
        assertEquals(2, all.get(0).getSeq());
        assertEquals(3, all.get(1).getSeq());
        assertEquals(5, all.get(2).getSeq());

        List<OrderbookUpdate> after2 = log.get(2);
        assertEquals(2, after2.size());
        assertEquals(3, after2.get(0).getSeq());
        assertEquals(5, after2.get(1).getSeq());

        assertTrue(log.get(5).isEmpty());
    }

    @Test
    void append_sameId_overwritesExisting() {
        OrderbookSeqLog log = new OrderbookSeqLog();

        log.append(new OrderbookUpdate(7, List.of(new PriceChange("A", 100, 1, Side.BID))));
        log.append(new OrderbookUpdate(7, List.of(new PriceChange("A", 101, 2, Side.BID))));

        List<OrderbookUpdate> got = log.get(-1);
        assertEquals(1, got.size());
        assertEquals(7, got.get(0).getSeq());
        assertEquals(101, got.get(0).getPriceChanges().get(0).getPrice());
        assertEquals(2, got.get(0).getPriceChanges().get(0).getVolume());
    }

    @Test
    void boundedRetention_evictsOldestSeq() {
        OrderbookSeqLog log = new OrderbookSeqLog(2);

        log.append(new OrderbookUpdate(1, List.of()));
        log.append(new OrderbookUpdate(2, List.of()));
        log.append(new OrderbookUpdate(3, List.of()));

        // Only seq 2 and 3 should remain
        assertEquals(2L, log.getMinSeq());

        List<OrderbookUpdate> all = log.get(-1);
        assertEquals(2, all.size());
        assertEquals(2, all.get(0).getSeq());
        assertEquals(3, all.get(1).getSeq());
    }

    @Test
    void nextSeqAndAppend_allocatesSeqAndStoresUpdate() {
        SeqGenerator seqGenerator = new SeqGenerator();
        seqGenerator.reset();

        OrderbookSeqLog log = new OrderbookSeqLog(10);
        long seq =
                log.nextSeqAndAppend(seqGenerator, List.of(new PriceChange("A", 101, 1, Side.BID)));

        assertEquals(0L, seq);
        List<OrderbookUpdate> got = log.get(-1);
        assertEquals(1, got.size());
        assertEquals(0L, got.get(0).getSeq());
        assertEquals(1, got.get(0).getPriceChanges().size());
    }
}
