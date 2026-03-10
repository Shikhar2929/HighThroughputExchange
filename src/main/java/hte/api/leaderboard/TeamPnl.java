package hte.api.leaderboard;

public class TeamPnl {
    private String username;
    private long pnl;

    public TeamPnl() {}

    public TeamPnl(String username, long pnl) {
        this.username = username;
        this.pnl = pnl;
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
}
