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

    public synchronized RoundResult saveRound(String roundName) {
        ArrayList<LeaderboardEntry> entries = adminService.getLeaderboard();
        List<TeamPnl> results = new ArrayList<>();
        for (LeaderboardEntry entry : entries) {
            results.add(new TeamPnl(entry.getUsername(), (long) entry.getBalance()));
        }

        RoundResult round = new RoundResult(roundName, System.currentTimeMillis(), results);
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

        List<TeamPnl> round1 = new ArrayList<>();
        for (String team : teamNames) {
            long pnl = (rng.nextInt(40001) - 10000);
            round1.add(new TeamPnl(team, pnl));
        }
        rounds.add(new RoundResult("Round 1", System.currentTimeMillis() - 86400000, round1));

        List<TeamPnl> round2 = new ArrayList<>();
        for (String team : teamNames) {
            long pnl = (rng.nextInt(40001) - 10000);
            round2.add(new TeamPnl(team, pnl));
        }
        rounds.add(new RoundResult("Round 2", System.currentTimeMillis(), round2));

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
