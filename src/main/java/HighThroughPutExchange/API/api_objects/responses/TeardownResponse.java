package HighThroughPutExchange.API.api_objects.responses;

public class TeardownResponse {
    private String message;


    public TeardownResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
