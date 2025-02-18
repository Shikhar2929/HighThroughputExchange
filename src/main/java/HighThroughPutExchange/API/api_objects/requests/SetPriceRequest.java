package HighThroughPutExchange.API.api_objects.requests;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class SetPriceRequest extends BaseAdminRequest {
    @NotNull
    private Map<String, Double> prices;
    public SetPriceRequest(String adminUsername, String adminPassword, Map<String, Double> prices) {
        super(adminUsername, adminPassword);
        this.prices = prices;
    }
    public Map<String, Double> getPrices() {
        return prices;
    }
}
