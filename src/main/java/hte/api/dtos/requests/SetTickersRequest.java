package hte.api.dtos.requests;

import jakarta.validation.constraints.NotNull;

public class SetTickersRequest extends BaseAdminRequest {
    @NotNull private String[] tickers;

    public SetTickersRequest(String adminUsername, String adminPassword, String[] tickers) {
        super(adminUsername, adminPassword);
        this.tickers = tickers;
    }

    public String[] getTickers() {
        return tickers;
    }
}
