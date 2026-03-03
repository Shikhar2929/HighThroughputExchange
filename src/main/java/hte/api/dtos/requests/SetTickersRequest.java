package hte.api.dtos.requests;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class SetTickersRequest extends BaseAdminRequest {
    @NotNull private Map<String, Integer> tickers;

    public SetTickersRequest(
            String adminUsername, String adminPassword, Map<String, Integer> tickers) {
        super(adminUsername, adminPassword);
        this.tickers = tickers;
    }

    public Map<String, Integer> getTickers() {
        return tickers;
    }
}
