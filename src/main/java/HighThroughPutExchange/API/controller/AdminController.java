package HighThroughPutExchange.api.controller;

import HighThroughPutExchange.api.State;
import HighThroughPutExchange.api.dtos.requests.*;
import HighThroughPutExchange.api.dtos.responses.*;
import HighThroughPutExchange.api.entities.User;
import HighThroughPutExchange.api.service.AdminService;
import HighThroughPutExchange.api.service.AuthService;
import HighThroughPutExchange.common.Message;
import HighThroughPutExchange.matchingengine.LeaderboardEntry;
import jakarta.validation.Valid;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
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
}
