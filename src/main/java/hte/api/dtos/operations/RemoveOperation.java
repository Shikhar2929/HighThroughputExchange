package hte.api.dtos.operations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class RemoveOperation extends Operation {
    @NotNull private long orderId;

    public RemoveOperation(@JsonProperty("orderId") long orderId) {
        super("remove");
        this.orderId = orderId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
}
