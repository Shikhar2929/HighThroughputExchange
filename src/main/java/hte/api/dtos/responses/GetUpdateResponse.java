package hte.api.dtos.responses;

import hte.common.OrderbookUpdate;

public class GetUpdateResponse extends AbstractMessageResponse {
    private long seq;
    private OrderbookUpdate update;

    public GetUpdateResponse(String message) {
        this(message, -1, null);
    }

    public GetUpdateResponse(String message, long seq, OrderbookUpdate update) {
        super(message);
        this.seq = seq;
        this.update = update;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public OrderbookUpdate getUpdate() {
        return update;
    }

    public void setUpdate(OrderbookUpdate update) {
        this.update = update;
    }
}
