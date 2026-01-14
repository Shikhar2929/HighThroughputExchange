package HighThroughPutExchange.Common;

import HighThroughPutExchange.MatchingEngine.PriceChange;
import java.util.List;
import java.util.Objects;

public class OrderbookUpdate {
    private final long updateId;
    private final List<PriceChange> priceChanges;

    public OrderbookUpdate(long updateId, List<PriceChange> priceChanges) {
        this.updateId = updateId;
        this.priceChanges = List.copyOf(Objects.requireNonNull(priceChanges));
    }

    public long getUpdateId() {
        return updateId;
    }

    public List<PriceChange> getPriceChanges() {
        return priceChanges;
    }
}
