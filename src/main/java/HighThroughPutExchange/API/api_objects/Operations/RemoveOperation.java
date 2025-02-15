package HighThroughPutExchange.API.api_objects.Operations;

import jakarta.validation.constraints.NotNull;

public class RemoveOperation extends Operation{
    @NotNull
    private long orderId;
    public RemoveOperation(long orderId) {
        super("remove");
        this.orderId = orderId;
    }
    public long getOrderId() {
        return orderId;
    }

}
