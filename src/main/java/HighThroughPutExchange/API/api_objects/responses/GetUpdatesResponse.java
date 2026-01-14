package HighThroughPutExchange.API.api_objects.responses;

import HighThroughPutExchange.Common.OrderbookUpdate;
import java.util.List;

public class GetUpdatesResponse {
    private long from;
    private long latestId;
    private List<OrderbookUpdate> updates;

    public GetUpdatesResponse(long from, long latestId, List<OrderbookUpdate> updates) {
        this.from = from;
        this.latestId = latestId;
        this.updates = updates;
    }

    public long getFromExclusive() {
        return from;
    }

    public void setFromExclusive(long from) {
        this.from = from;
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
