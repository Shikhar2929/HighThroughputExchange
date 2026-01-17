package HighThroughPutExchange.API.api_objects.responses;

import HighThroughPutExchange.Common.OrderbookUpdate;
import java.util.List;

public class GetUpdatesResponse {
    private long from;
    private long latestSeq;
    private List<OrderbookUpdate> updates;

    public GetUpdatesResponse(long from, long latestSeq, List<OrderbookUpdate> updates) {
        this.from = from;
        this.latestSeq = latestSeq;
        this.updates = updates;
    }

    public long getFromExclusive() {
        return from;
    }

    public void setFromExclusive(long from) {
        this.from = from;
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
