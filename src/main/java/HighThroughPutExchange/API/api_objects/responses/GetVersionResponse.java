package HighThroughPutExchange.API.api_objects.responses;

public class GetVersionResponse {
    private long version;

    public GetVersionResponse(long version) {
        this.version = version;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
