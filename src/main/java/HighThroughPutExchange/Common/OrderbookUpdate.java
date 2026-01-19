package HighThroughPutExchange.common;

import HighThroughPutExchange.matchingengine.PriceChange;
import java.util.List;
import java.util.Objects;

public class OrderbookUpdate {
    private final long seq;
    private final List<PriceChange> priceChanges;

    public OrderbookUpdate(long seq, List<PriceChange> priceChanges) {
        this.seq = seq;
        this.priceChanges = List.copyOf(Objects.requireNonNull(priceChanges));
    }

    public long getSeq() {
        return seq;
    }

    public List<PriceChange> getPriceChanges() {
        return priceChanges;
    }
}
