package HighThroughPutExchange.API.api_objects.responses;

public class RemoveAllResponse {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RemoveAllResponse(String message) {
        this.message = message;
    }
}
