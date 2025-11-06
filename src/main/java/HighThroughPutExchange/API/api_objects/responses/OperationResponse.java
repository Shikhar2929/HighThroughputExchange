package HighThroughPutExchange.API.api_objects.responses;

public class OperationResponse extends AbstractMessageResponse {
    private String type;

    public OperationResponse(String type, String message) {
        super(message);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
