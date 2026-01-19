package HighThroughPutExchange.api.dtos.requests;

public class PrivatePageRequest extends BasePrivateRequest {
    public PrivatePageRequest(String username, String sessionToken) {
        super(username, sessionToken);
    }
}
