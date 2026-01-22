package hte.api.controller;

import hte.api.auth.RateLimiter;
import hte.api.dtos.requests.AdminDashboardRequest;
import hte.api.dtos.requests.PrivatePageRequest;
import hte.api.dtos.responses.AdminDashboardResponse;
import hte.api.dtos.responses.PrivatePageResponse;
import hte.api.service.AuthService;
import hte.common.Message;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
