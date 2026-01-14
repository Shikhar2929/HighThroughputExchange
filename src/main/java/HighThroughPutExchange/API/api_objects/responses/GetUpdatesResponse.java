package HighThroughPutExchange.API.api_objects.responses;

import HighThroughPutExchange.Common.OrderbookUpdate;
import java.util.List;

public class GetUpdatesResponse {
    private long fromExclusive;
    private long latestId;
    private List<OrderbookUpdate> updates;

    public GetUpdatesResponse(long fromExclusive, long latestId, List<OrderbookUpdate> updates) {
        this.fromExclusive = fromExclusive;
        this.latestId = latestId;
        this.updates = updates;
    }

    public long getFromExclusive() {
        return fromExclusive;
    }

    public void setFromExclusive(long fromExclusive) {
        this.fromExclusive = fromExclusive;
    }

    public long getLatestId() {
        return latestId;
    }

    public void setLatestId(long latestId) {
        this.latestId = latestId;
    }

    public List<OrderbookUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<OrderbookUpdate> updates) {
        this.updates = updates;
    }
}
