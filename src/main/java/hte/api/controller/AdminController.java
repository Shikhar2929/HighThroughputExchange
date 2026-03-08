package hte.api.controller;

import hte.api.State;
import hte.api.dtos.requests.AddBotRequest;
import hte.api.dtos.requests.AddUserRequest;
import hte.api.dtos.requests.LeaderboardRequest;
import hte.api.dtos.requests.SetMaxOrderPriceRequest;
import hte.api.dtos.requests.SetPriceRequest;
import hte.api.dtos.requests.SetStateRequest;
import hte.api.dtos.requests.SetTickersRequest;
import hte.api.dtos.requests.ShutdownRequest;
import hte.api.dtos.responses.AddUserResponse;
import hte.api.dtos.responses.LeaderboardResponse;
import hte.api.dtos.responses.SetMaxOrderPriceResponse;
import hte.api.dtos.responses.SetPriceResponse;
import hte.api.dtos.responses.SetStateResponse;
import hte.api.dtos.responses.SetTickersResponse;
import hte.api.dtos.responses.ShutdownResponse;
import hte.api.entities.User;
import hte.api.service.AdminService;
import hte.api.service.AuthService;
import hte.common.Message;
import hte.common.SeqGenerator;
import hte.matchingengine.LeaderboardEntry;
import hte.matchingengine.MatchingEngine;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;
    private final AuthService authService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SeqGenerator seqGenerator;
    private final MatchingEngine matchingEngine;

    public AdminController(
            AdminService adminService,
            AuthService authService,
            SimpMessagingTemplate messagingTemplate,
            SeqGenerator seqGenerator,
            MatchingEngine matchingEngine) {
        this.adminService = adminService;
        this.authService = authService;
        this.messagingTemplate = messagingTemplate;
        this.seqGenerator = seqGenerator;
        this.matchingEngine = matchingEngine;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/add_user")
    public ResponseEntity<AddUserResponse> addUser(@Valid @RequestBody AddUserRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new AddUserResponse(Message.AUTHENTICATION_FAILED.toString(), ""),
                    HttpStatus.UNAUTHORIZED);
        }

        if (adminService.usernameExists(form.getUsername())) {
            return new ResponseEntity<>(
                    new AddUserResponse("Username already exists.", ""), HttpStatus.BAD_REQUEST);
        }

        User user = adminService.addUser(form.getUsername(), form.getName(), form.getEmail());
        return new ResponseEntity<>(
                new AddUserResponse(
                        Message.SUCCESS.toString(), user.getApiKey(), user.getApiKey2()),
                HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/add_bot")
    public ResponseEntity<AddUserResponse> addBot(@Valid @RequestBody AddBotRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new AddUserResponse(Message.AUTHENTICATION_FAILED.toString(), ""),
                    HttpStatus.UNAUTHORIZED);
        }
        if (adminService.usernameExists(form.getUsername())) {
            return new ResponseEntity<>(
                    new AddUserResponse("Username already exists.", ""), HttpStatus.BAD_REQUEST);
        }
        String key = adminService.addBot(form.getUsername());
        return new ResponseEntity<>(
                new AddUserResponse(Message.SUCCESS.toString(), key), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/shutdown")
    public ResponseEntity<ShutdownResponse> shutdown(@Valid @RequestBody ShutdownRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new ShutdownResponse(Message.AUTHENTICATION_FAILED.toString()),
                    HttpStatus.UNAUTHORIZED);
        }

        adminService.shutdown();
        return new ResponseEntity<>(
                new ShutdownResponse(Message.SUCCESS.toString()), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponse> leaderboard(
            @Valid @RequestBody LeaderboardRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new LeaderboardResponse(Message.AUTHENTICATION_FAILED.toString(), null),
                    HttpStatus.UNAUTHORIZED);
        }

        ArrayList<LeaderboardEntry> data = adminService.getLeaderboard();
        return new ResponseEntity<>(
                new LeaderboardResponse(Message.SUCCESS.toString(), data), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/set_state")
    public ResponseEntity<SetStateResponse> setState(@Valid @RequestBody SetStateRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new SetStateResponse(Message.AUTHENTICATION_FAILED.toString(), 0),
                    HttpStatus.UNAUTHORIZED);
        }

        if (form.getTargetState() >= State.values().length || form.getTargetState() < 0) {
            return new ResponseEntity<>(
                    new SetStateResponse(Message.BAD_INPUT.toString(), 0), HttpStatus.BAD_REQUEST);
        }

        int newStateOrdinal = adminService.applyState(form.getTargetState());
        return new ResponseEntity<>(
                new SetStateResponse(Message.SUCCESS.toString(), newStateOrdinal), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/set_price")
    public ResponseEntity<SetPriceResponse> setPrice(@Valid @RequestBody SetPriceRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new SetPriceResponse(Message.AUTHENTICATION_FAILED.toString()),
                    HttpStatus.UNAUTHORIZED);
        }
        String message = adminService.setPrice(form.getPrices());
        return new ResponseEntity<>(new SetPriceResponse(message), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/set_max_order_price")
    public ResponseEntity<SetMaxOrderPriceResponse> setMaxOrderPrice(
            @Valid @RequestBody SetMaxOrderPriceRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new SetMaxOrderPriceResponse(Message.AUTHENTICATION_FAILED.toString(), 0),
                    HttpStatus.UNAUTHORIZED);
        }

        try {
            int applied = adminService.setMaxOrderPrice(form.getMaxPrice());
            return new ResponseEntity<>(
                    new SetMaxOrderPriceResponse(Message.SUCCESS.toString(), applied),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new SetMaxOrderPriceResponse(Message.BAD_INPUT.toString(), 0),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/set_tickers")
    public ResponseEntity<SetTickersResponse> setTickers(
            @Valid @RequestBody SetTickersRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new SetTickersResponse(Message.AUTHENTICATION_FAILED.toString(), null),
                    HttpStatus.UNAUTHORIZED);
        }

        String[] tickers = adminService.setTickers(form.getTickers());
        if (tickers == null) {
            return new ResponseEntity<>(
                    new SetTickersResponse(Message.BAD_INPUT.toString(), null),
                    HttpStatus.BAD_REQUEST);
        }

        // Broadcast a snapshot reset event to all connected WebSocket clients so that
        // every user's UI immediately replaces old tickers with the new ones and resets
        // the order book.
        try {
            String snapshotJson = matchingEngine.serializeOrderBooks();
            long latestSeq = seqGenerator.get();

            Map<String, Object> resetPayload = new HashMap<>();
            resetPayload.put("type", "snapshot_reset");
            resetPayload.put("snapshot", snapshotJson);
            resetPayload.put("latestSeq", latestSeq);
            resetPayload.put("tickers", tickers);

            messagingTemplate.convertAndSend("/topic/orderbook", resetPayload);
        } catch (Exception e) {
            logger.warn("Failed to broadcast snapshot reset after set_tickers", e);
        }

        return new ResponseEntity<>(
                new SetTickersResponse(Message.SUCCESS.toString(), tickers), HttpStatus.OK);
    }
}
