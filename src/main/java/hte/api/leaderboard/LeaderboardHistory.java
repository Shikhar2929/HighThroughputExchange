package hte.api.leaderboard;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardHistory {
    private List<RoundResult> rounds;

    public LeaderboardHistory() {
        this.rounds = new ArrayList<>();
    }

    public LeaderboardHistory(List<RoundResult> rounds) {
        this.rounds = rounds;
    }

    public List<RoundResult> getRounds() {
        return rounds;
    }

    public void setRounds(List<RoundResult> rounds) {
        this.rounds = rounds;
    }
}
