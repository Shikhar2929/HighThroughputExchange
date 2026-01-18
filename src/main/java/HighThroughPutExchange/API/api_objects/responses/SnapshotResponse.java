package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class SnapshotResponse extends AbstractMessageResponse {
    @JsonRawValue private String snapshot;
    private long latestSeq;

    public SnapshotResponse(String message) {
        this(message, null, -1);
    }

    public SnapshotResponse(String message, String snapshot, long latestSeq) {
        super(message);
        this.snapshot = snapshot;
        this.latestSeq = latestSeq;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public long getLatestSeq() {
        return latestSeq;
    }

    public void setLatestSeq(long latestSeq) {
        this.latestSeq = latestSeq;
    }
}
