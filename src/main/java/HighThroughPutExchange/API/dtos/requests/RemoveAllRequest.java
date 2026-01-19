package HighThroughPutExchange.api.dtos.requests;

public class RemoveAllRequest extends BasePrivateRequest {
    RemoveAllRequest(String username, String sessionToken) {
        super(username, sessionToken);
    }
}
