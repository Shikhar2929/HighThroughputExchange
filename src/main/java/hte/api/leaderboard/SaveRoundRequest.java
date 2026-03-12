package hte.api.leaderboard;

import hte.api.dtos.requests.BaseAdminRequest;
import jakarta.validation.constraints.NotNull;

public class SaveRoundRequest extends BaseAdminRequest {
    @NotNull private String roundName;

    /** Round constant C (free money / taker bot loss). If null, 1.0 is used for scoring. */
    private Double c;

    public SaveRoundRequest() {
        super(null, null);
    }

    public SaveRoundRequest(String adminUsername, String adminPassword, String roundName) {
        super(adminUsername, adminPassword);
        this.roundName = roundName;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public Double getC() {
        return c;
    }

    public void setC(Double c) {
        this.c = c;
    }
}
