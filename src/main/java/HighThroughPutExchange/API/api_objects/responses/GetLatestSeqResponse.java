package HighThroughPutExchange.API.api_objects.responses;

public class GetLatestSeqResponse {
    private long latestSeq;

    public GetLatestSeqResponse(long latestSeq) {
        this.latestSeq = latestSeq;
    }

    public long getLatestSeq() {
        return latestSeq;
    }

    public void setLatestSeq(long latestSeq) {
        this.latestSeq = latestSeq;
    }
}
