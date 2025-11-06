package HighThroughPutExchange.API.api_objects.requests;

public class RemoveAllRequest extends BasePrivateRequest {
    RemoveAllRequest(String username, String sessionToken) {
        super(username, sessionToken);
    }
}
