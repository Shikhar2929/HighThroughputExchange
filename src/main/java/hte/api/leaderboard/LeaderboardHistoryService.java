package hte.api.leaderboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import hte.api.service.AdminService;
import hte.matchingengine.LeaderboardEntry;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardHistoryService.class);

    private final AdminService adminService;
    private final ObjectMapper objectMapper;
    private final File storageFile;
    private LeaderboardHistory history;

    public LeaderboardHistoryService(
            AdminService adminService,
            @Value("${hte.leaderboard.history-file:leaderboard_history.json}")
                    String historyFilePath) {
        this.adminService = adminService;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.storageFile = new File(historyFilePath);
        this.history = loadFromDisk();
    }

    public synchronized LeaderboardHistory getHistory() {
        return history;
    }

    /**
     * Utility score U(x) for one round: U(x) = ln(1 + 35*x/C) if x>=0, else 35*x/C. C is the round
     * constant (free money / taker bot loss). Encourages risk-averse behavior.
     */
    private static double utilityScore(long pnl, double c) {
        double safeC = Math.min(c, 1000.0);
        double x = (double) pnl;
        if (x >= 0) {
            return Math.log(1.0 + 35.0 * x / safeC);
        } else {
            return 35.0 * x / safeC;
        }
    }

    public synchronized RoundResult saveRound(String roundName, double c) {
        ArrayList<LeaderboardEntry> entries = adminService.getLeaderboard();
        List<TeamPnl> results = new ArrayList<>();
        for (LeaderboardEntry entry : entries) {
            long pnl = (long) entry.getBalance();
            double score = utilityScore(pnl, c);
            results.add(new TeamPnl(entry.getUsername(), pnl, score));
        }

        RoundResult round = new RoundResult(roundName, System.currentTimeMillis(), results, c);
        history.getRounds().add(round);
        saveToDisk();
        return round;
    }

    public synchronized void clearHistory() {
        history = new LeaderboardHistory();
        saveToDisk();
    }

    /**
     * Replaces history with sample data: 35 teams and 2 rounds of fake PnL. For testing the public
     * leaderboard display only.
     */
    public synchronized void seedSampleData() {
        Random rng = new Random(42);
        int numTeams = 35;
        List<String> teamNames = new ArrayList<>();
        for (int i = 1; i <= numTeams; i++) {
            teamNames.add(String.format("Team %02d", i));
        }

        List<RoundResult> rounds = new ArrayList<>();
        double sampleC = 10000.0;

        List<TeamPnl> round1 = new ArrayList<>();
        for (String team : teamNames) {
            long pnl = (rng.nextInt(40001) - 10000);
            double score = utilityScore(pnl, sampleC);
            round1.add(new TeamPnl(team, pnl, score));
        }
        rounds.add(
                new RoundResult("Round 1", System.currentTimeMillis() - 86400000, round1, sampleC));

        List<TeamPnl> round2 = new ArrayList<>();
        for (String team : teamNames) {
            long pnl = (rng.nextInt(40001) - 10000);
            double score = utilityScore(pnl, sampleC);
            round2.add(new TeamPnl(team, pnl, score));
        }
        rounds.add(new RoundResult("Round 2", System.currentTimeMillis(), round2, sampleC));

        history = new LeaderboardHistory(rounds);
        saveToDisk();
    }

    private LeaderboardHistory loadFromDisk() {
        if (!storageFile.exists()) {
            return new LeaderboardHistory();
        }
        try {
            return objectMapper.readValue(storageFile, LeaderboardHistory.class);
        } catch (IOException e) {
            logger.warn("Failed to load leaderboard history from {}", storageFile, e);
            return new LeaderboardHistory();
        }
    }

    private void saveToDisk() {
        try {
            objectMapper.writeValue(storageFile, history);
        } catch (IOException e) {
            logger.error("Failed to save leaderboard history to {}", storageFile, e);
        }
    }
}
