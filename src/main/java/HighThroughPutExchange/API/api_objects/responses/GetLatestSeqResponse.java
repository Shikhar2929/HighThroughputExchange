package HighThroughPutExchange.API.api_objects.responses;

public class GetLatestSeqResponse extends AbstractMessageResponse {
    private long latestSeq;

    public GetLatestSeqResponse(String message, long latestSeq) {
        super(message);
        this.latestSeq = latestSeq;
    }

    public long getLatestSeq() {
        return latestSeq;
    }

    public void setLatestSeq(long latestSeq) {
        this.latestSeq = latestSeq;
    }
}
