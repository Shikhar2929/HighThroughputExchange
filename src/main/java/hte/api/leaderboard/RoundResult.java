package hte.api.leaderboard;

import java.util.List;

public class RoundResult {
    private String roundName;
    private long timestamp;
    private List<TeamPnl> results;

    /**
     * Round constant C (free money / taker bot loss). Used for utility score. Default 1.0 if
     * absent.
     */
    private double c = 1.0;

    public RoundResult() {}

    public RoundResult(String roundName, long timestamp, List<TeamPnl> results) {
        this.roundName = roundName;
        this.timestamp = timestamp;
        this.results = results;
    }

    public RoundResult(String roundName, long timestamp, List<TeamPnl> results, double c) {
        this.roundName = roundName;
        this.timestamp = timestamp;
        this.results = results;
        this.c = c;
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

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }
}
