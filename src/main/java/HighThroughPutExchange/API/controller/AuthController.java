package HighThroughPutExchange.API.controller;

import HighThroughPutExchange.API.api_objects.requests.AdminDashboardRequest;
import HighThroughPutExchange.API.api_objects.requests.PrivatePageRequest;
import HighThroughPutExchange.API.api_objects.responses.AdminDashboardResponse;
import HighThroughPutExchange.API.api_objects.responses.PrivatePageResponse;
import HighThroughPutExchange.API.authentication.RateLimiter;
import HighThroughPutExchange.API.service.AuthService;
import HighThroughPutExchange.Common.Message;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService authService;
    private final RateLimiter rateLimiter;

    public AuthController(AuthService authService, RateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/admin_page")
    public ResponseEntity<AdminDashboardResponse> adminPage(
            @Valid @RequestBody AdminDashboardRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new AdminDashboardResponse(Message.AUTHENTICATION_FAILED.toString(), ""),
                    HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(
                new AdminDashboardResponse(Message.SUCCESS.toString(), ""), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/privatePage")
    public ResponseEntity<PrivatePageResponse> privatePage(
            @Valid @RequestBody PrivatePageRequest form) {
        if (!authService.authenticatePrivate(form)) {
            return new ResponseEntity<>(
                    new PrivatePageResponse(Message.AUTHENTICATION_FAILED.toString()),
                    HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimiter.processRequest(form)) {
            return new ResponseEntity<>(
                    new PrivatePageResponse(Message.RATE_LIMITED.toString()),
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        return new ResponseEntity<>(
                new PrivatePageResponse(Message.SUCCESS.toString()), HttpStatus.OK);
    }
}
