package HighThroughPutExchange.API.api_objects.responses;

import HighThroughPutExchange.MatchingEngine.LeaderboardEntry;
import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.List;

public class LeaderboardResponse {
    @JsonRawValue
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private List<LeaderboardEntry> data;

    public LeaderboardResponse() {}

    public LeaderboardResponse(String message, List<LeaderboardEntry> data) {
        this.message = message;
        this.data = data;
    }

    public void setData(List<LeaderboardEntry> data) {
        this.data = data;
    }

    public List<LeaderboardEntry> getData() {
        return data;
    }
}
