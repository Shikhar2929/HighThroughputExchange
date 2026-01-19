package hte.api.dtos.requests;

public class TeardownRequest extends PrivatePageRequest {
    public TeardownRequest(String username, String sessionToken) {
        super(username, sessionToken);
    }
}
