package hte.api.leaderboard;

import hte.api.service.AuthService;
import hte.common.Message;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeaderboardHistoryController {

    private final LeaderboardHistoryService historyService;
    private final AuthService authService;

    public LeaderboardHistoryController(
            LeaderboardHistoryService historyService, AuthService authService) {
        this.historyService = historyService;
        this.authService = authService;
    }

    /** Public endpoint: no auth required. Used by the public leaderboard page. */
    @CrossOrigin(origins = "*")
    @GetMapping("/leaderboard_history")
    public ResponseEntity<LeaderboardHistory> getHistory() {
        return new ResponseEntity<>(historyService.getHistory(), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/save_round")
    public ResponseEntity<Map<String, Object>> saveRound(
            @Valid @RequestBody SaveRoundRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    Map.of("message", Message.AUTHENTICATION_FAILED.toString()),
                    HttpStatus.UNAUTHORIZED);
        }

        RoundResult round = historyService.saveRound(form.getRoundName());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", Message.SUCCESS.toString());
        body.put("round", round);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/clear_leaderboard_history")
    public ResponseEntity<Map<String, String>> clearHistory(
            @Valid @RequestBody hte.api.dtos.requests.BaseAdminRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    Map.of("message", Message.AUTHENTICATION_FAILED.toString()),
                    HttpStatus.UNAUTHORIZED);
        }

        historyService.clearHistory();
        return new ResponseEntity<>(Map.of("message", Message.SUCCESS.toString()), HttpStatus.OK);
    }

    /** Admin-only. Seeds leaderboard history with 35 fake teams and 2 rounds for testing. */
    @CrossOrigin(origins = "*")
    @PostMapping("/seed_leaderboard_sample")
    public ResponseEntity<Map<String, String>> seedSample(
            @Valid @RequestBody hte.api.dtos.requests.BaseAdminRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    Map.of("message", Message.AUTHENTICATION_FAILED.toString()),
                    HttpStatus.UNAUTHORIZED);
        }

        historyService.seedSampleData();
        return new ResponseEntity<>(Map.of("message", Message.SUCCESS.toString()), HttpStatus.OK);
    }
}
