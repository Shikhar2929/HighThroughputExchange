package HighThroughPutExchange.API.api_objects.responses;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class SnapshotResponse {
    @JsonRawValue
    private String snapshot;
    private long version;

    public SnapshotResponse(String snapshot, long version) {
        this.snapshot = snapshot;
        this.version = version;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
