package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;

public class RemoveRequest extends BasePrivateRequest {
    @NotNull
    long orderId;

    RemoveRequest(String username, String apiKey, long orderId) {
        super(username, apiKey);
        this.orderId = orderId;
    }

    public long getOrderID() {
        return orderId;
    }
}
