package hte.api.leaderboard;

public class TeamPnl {
    private String username;
    private long pnl;
    private double score;

    public TeamPnl() {}

    public TeamPnl(String username, long pnl) {
        this.username = username;
        this.pnl = pnl;
        this.score = 0.0;
    }

    public TeamPnl(String username, long pnl, double score) {
        this.username = username;
        this.pnl = pnl;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getPnl() {
        return pnl;
    }

    public void setPnl(long pnl) {
        this.pnl = pnl;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
