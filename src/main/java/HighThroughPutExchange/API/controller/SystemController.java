package HighThroughPutExchange.api.controller;

import HighThroughPutExchange.api.ServerApplication;
import HighThroughPutExchange.api.auth.RateLimiter;
import HighThroughPutExchange.api.dtos.requests.PrivatePageRequest;
import HighThroughPutExchange.api.dtos.responses.GetDetailsResponse;
import HighThroughPutExchange.api.service.AuthService;
import HighThroughPutExchange.api.service.SystemService;
import HighThroughPutExchange.common.Message;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {
    private final AuthService authService;
    private final SystemService systemService;
    private final RateLimiter rateLimiter;
    private final ServerApplication app;

    public SystemController(
            ServerApplication app,
            AuthService authService,
            SystemService systemService,
            RateLimiter rateLimiter) {
        this.app = app;
        this.authService = authService;
        this.systemService = systemService;
        this.rateLimiter = rateLimiter;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/get_state")
    public ResponseEntity<String> state() {
        return new ResponseEntity<>(
                String.format("{\"state\": %d}", app.getState().ordinal()), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/get_details")
    public ResponseEntity<GetDetailsResponse> getDetails(
            @Valid @RequestBody PrivatePageRequest form) {
        if (!authService.authenticatePrivate(form)) {
            return new ResponseEntity<>(
                    new GetDetailsResponse(Message.AUTHENTICATION_FAILED.toString(), ""),
                    HttpStatus.UNAUTHORIZED);
        }

        if (!rateLimiter.processRequest(form)) {
            return new ResponseEntity<>(
                    new GetDetailsResponse(Message.RATE_LIMITED.toString(), ""),
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        String details = systemService.getUserDetails(form.getUsername());
        return new ResponseEntity<>(
                new GetDetailsResponse(Message.SUCCESS.toString(), details), HttpStatus.OK);
    }
}
