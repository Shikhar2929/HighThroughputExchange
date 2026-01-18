package HighThroughPutExchange.API.api_objects.responses;

import HighThroughPutExchange.Common.OrderbookUpdate;
import java.util.List;

public class GetUpdatesResponse {
    private long fromExclusive;
    private long latestSeq;
    private List<OrderbookUpdate> updates;

    public GetUpdatesResponse(long fromExclusive, long latestSeq, List<OrderbookUpdate> updates) {
        this.fromExclusive = fromExclusive;
        this.latestSeq = latestSeq;
        this.updates = updates;
    }

    public long getFromExclusive() {
        return fromExclusive;
    }

    public void setFromExclusive(long fromExclusive) {
        this.fromExclusive = fromExclusive;
    }

    public long getLatestSeq() {
        return latestSeq;
    }

    public void setLatestSeq(long latestSeq) {
        this.latestSeq = latestSeq;
    }

    public List<OrderbookUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<OrderbookUpdate> updates) {
        this.updates = updates;
    }
}
