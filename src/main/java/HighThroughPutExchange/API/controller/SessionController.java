package HighThroughPutExchange.API.controller;

import HighThroughPutExchange.API.api_objects.requests.BuildupRequest;
import HighThroughPutExchange.API.api_objects.requests.TeardownRequest;
import HighThroughPutExchange.API.api_objects.responses.BuildupResponse;
import HighThroughPutExchange.API.api_objects.responses.TeardownResponse;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import HighThroughPutExchange.API.service.SessionService;
import HighThroughPutExchange.Common.Message;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SessionController {

    private final SessionService sessionService;
    private final PrivatePageAuthenticator privatePageAuthenticator;

    public SessionController(SessionService sessionService, PrivatePageAuthenticator privatePageAuthenticator) {
        this.sessionService = sessionService;
        this.privatePageAuthenticator = privatePageAuthenticator;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/buildup")
    public ResponseEntity<BuildupResponse> buildup(@Valid @RequestBody BuildupRequest form) {
        BuildupResponse resp = sessionService.buildup(form.getUsername(), form.getApiKey());
        if (!Message.SUCCESS.toString().equals(resp.getMessage())) {
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/bot_buildup")
    public ResponseEntity<BuildupResponse> botBuildup(@Valid @RequestBody BuildupRequest form) {
        BuildupResponse resp = sessionService.botBuildup(form.getUsername(), form.getApiKey());
        if (!Message.SUCCESS.toString().equals(resp.getMessage())) {
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/teardown")
    public ResponseEntity<TeardownResponse> teardown(@Valid @RequestBody TeardownRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new TeardownResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        }
        TeardownResponse resp = sessionService.teardown(form.getUsername());
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
