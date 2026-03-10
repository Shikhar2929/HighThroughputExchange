package hte.api.leaderboard;

import hte.api.dtos.requests.BaseAdminRequest;
import jakarta.validation.constraints.NotNull;

public class SaveRoundRequest extends BaseAdminRequest {
    @NotNull private String roundName;

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
}
