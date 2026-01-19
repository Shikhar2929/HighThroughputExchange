package hte.api.controller;

import hte.api.ServerApplication;
import hte.api.State;
import hte.api.auth.RateLimiter;
import hte.api.dtos.requests.BaseAdminRequest;
import hte.api.dtos.requests.BidAuctionRequest;
import hte.api.dtos.responses.BidAuctionResponse;
import hte.api.dtos.responses.GetLeadingAuctionBidResponse;
import hte.api.service.AuctionService;
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
public class AuctionController {

    private final AuctionService auctionService;
    private final AuthService authService;
    private final ServerApplication app;
    private final RateLimiter rateLimiter;

    public AuctionController(
            AuctionService auctionService,
            AuthService authService,
            ServerApplication app,
            RateLimiter rateLimiter) {
        this.auctionService = auctionService;
        this.authService = authService;
        this.app = app;
        this.rateLimiter = rateLimiter;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/get_leading_auction_bid")
    public ResponseEntity<GetLeadingAuctionBidResponse> getLeadingAuctionBid(
            @Valid @RequestBody BaseAdminRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new GetLeadingAuctionBidResponse(Message.AUTHENTICATION_FAILED.toString()),
                    HttpStatus.UNAUTHORIZED);
        }

        GetLeadingAuctionBidResponse message = auctionService.getLeadingAuctionBid();
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /*
     * Method with the following behavior: 1. sets state to STOP, stopping the
     * auctioning phase 2. gets best bid by best user 3. executes auction 4. resets
     * auction object 5. returns who won auction and at what amount
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/terminate_auction")
    public ResponseEntity<GetLeadingAuctionBidResponse> terminateAuction(
            @Valid @RequestBody BaseAdminRequest form) {
        if (!authService.authenticateAdmin(form)) {
            return new ResponseEntity<>(
                    new GetLeadingAuctionBidResponse(Message.AUTHENTICATION_FAILED.toString()),
                    HttpStatus.UNAUTHORIZED);
        }

        if (app.getState() != State.TRADE_WITH_AUCTION) {
            return new ResponseEntity<>(
                    new GetLeadingAuctionBidResponse(Message.AUTHENTICATION_FAILED.toString()),
                    HttpStatus.LOCKED);
        }

        app.setStateInternal(State.STOP);
        GetLeadingAuctionBidResponse message = auctionService.terminateAuction();
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/bid_auction")
    public ResponseEntity<BidAuctionResponse> bidAuction(
            @Valid @RequestBody BidAuctionRequest form) {
        if (!authService.authenticatePrivate(form)) {
            return new ResponseEntity<>(
                    new BidAuctionResponse(Message.AUTHENTICATION_FAILED.toString()),
                    HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimiter.processRequest(form)) {
            return new ResponseEntity<>(
                    new BidAuctionResponse(Message.RATE_LIMITED.toString()),
                    HttpStatus.TOO_MANY_REQUESTS);
        }
        if (app.getState() != State.TRADE_WITH_AUCTION) {
            return new ResponseEntity<>(
                    new BidAuctionResponse(Message.AUCTION_LOCKED.toString()), HttpStatus.LOCKED);
        }

        if (!auctionService.isValid(form.getUsername(), form.getBid())) {
            return new ResponseEntity<>(auctionService.bidAuctionInvalid(), HttpStatus.BAD_REQUEST);
        }

        BidAuctionResponse message = auctionService.bidAuction(form);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
