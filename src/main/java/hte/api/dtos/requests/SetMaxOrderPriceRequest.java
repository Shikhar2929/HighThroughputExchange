package hte.api.dtos.requests;

import jakarta.validation.constraints.NotNull;

public class SetMaxOrderPriceRequest extends BaseAdminRequest {
    @NotNull private int maxPrice;

    public SetMaxOrderPriceRequest(String adminUsername, String adminPassword, int maxPrice) {
        super(adminUsername, adminPassword);
        this.maxPrice = maxPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }
}
