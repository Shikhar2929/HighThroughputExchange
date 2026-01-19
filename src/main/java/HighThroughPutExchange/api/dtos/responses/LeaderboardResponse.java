package HighThroughPutExchange.api.dtos.responses;

import HighThroughPutExchange.matchingengine.LeaderboardEntry;
import java.util.List;

public class LeaderboardResponse extends AbstractMessageResponse {

    private List<LeaderboardEntry> data;

    public LeaderboardResponse() {}

    public LeaderboardResponse(String message, List<LeaderboardEntry> data) {
        super(message);
        this.data = data;
    }

    public void setData(List<LeaderboardEntry> data) {
        this.data = data;
    }

    public List<LeaderboardEntry> getData() {
        return data;
    }
}
