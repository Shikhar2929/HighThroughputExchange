package hte.api.leaderboard;

import java.util.List;

public class RoundResult {
    private String roundName;
    private long timestamp;
    private List<TeamPnl> results;

    public RoundResult() {}

    public RoundResult(String roundName, long timestamp, List<TeamPnl> results) {
        this.roundName = roundName;
        this.timestamp = timestamp;
        this.results = results;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<TeamPnl> getResults() {
        return results;
    }

    public void setResults(List<TeamPnl> results) {
        this.results = results;
    }
}
