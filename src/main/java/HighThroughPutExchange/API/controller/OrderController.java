package HighThroughPutExchange.API.controller;

import HighThroughPutExchange.API.ServerApplication;
import HighThroughPutExchange.API.State;
import HighThroughPutExchange.API.api_objects.requests.*;
import HighThroughPutExchange.API.api_objects.responses.*;
import HighThroughPutExchange.API.authentication.BotAuthenticator;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import HighThroughPutExchange.API.authentication.RateLimiter;
import HighThroughPutExchange.API.service.OrderService;
import HighThroughPutExchange.Common.Message;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final ServerApplication app;
    private final RateLimiter rateLimiter;
    private final PrivatePageAuthenticator privatePageAuthenticator;
    private final BotAuthenticator botAuthenticator;

    public OrderController(OrderService orderService, ServerApplication app, RateLimiter rateLimiter) {
        this.orderService = orderService;
        this.app = app;
        this.rateLimiter = rateLimiter;
        this.privatePageAuthenticator = PrivatePageAuthenticator.getInstance();
        this.botAuthenticator = BotAuthenticator.getInstance();
    }

    private boolean rateLimit(BasePrivateRequest form) {
        return rateLimiter.processRequest(form);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/limit_order")
    public ResponseEntity<LimitOrderResponse> limitOrder(@Valid @RequestBody LimitOrderRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new LimitOrderResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimit(form)) {
            return new ResponseEntity<>(new LimitOrderResponse(Message.RATE_LIMITED.toString()), HttpStatus.TOO_MANY_REQUESTS);
        }
        if (app.getState() == State.STOP) {
            return new ResponseEntity<>(new LimitOrderResponse(Message.TRADE_LOCKED.toString()), HttpStatus.LOCKED);
        }
        String message = orderService.placeLimitOrder(form.getUsername(), form.getTicker(), form.getPrice(), form.getVolume(), form.getBid());
        return new ResponseEntity<>(new LimitOrderResponse(message), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/bot_limit_order")
    public ResponseEntity<LimitOrderResponse> botLimitOrder(@Valid @RequestBody BotLimitOrderRequest form) {
        if (!botAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new LimitOrderResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        }
        if (app.getState() == State.STOP) {
            return new ResponseEntity<>(new LimitOrderResponse(Message.TRADE_LOCKED.toString()), HttpStatus.LOCKED);
        }
        String message = orderService.placeLimitOrder(form.getUsername(), form.getTicker(), form.getPrice(), form.getVolume(), form.getBid());
        return new ResponseEntity<>(new LimitOrderResponse(message), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/remove_all")
    public ResponseEntity<RemoveAllResponse> removeAll(@Valid @RequestBody RemoveAllRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimit(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.RATE_LIMITED.toString()), HttpStatus.TOO_MANY_REQUESTS);
        }
        if (app.getState() == State.STOP) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.TRADE_LOCKED.toString()), HttpStatus.LOCKED);
        }
        if (form.getUsername() == null)
            return new ResponseEntity<>(new RemoveAllResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        String message = orderService.removeAll(form.getUsername());
        return new ResponseEntity<>(new RemoveAllResponse(message), HttpStatus.OK);
    }

    @PostMapping("/bot_remove_all")
    public ResponseEntity<RemoveAllResponse> botRemoveAll(@Valid @RequestBody RemoveAllRequest form) {
        if (!botAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimit(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.RATE_LIMITED.toString()), HttpStatus.TOO_MANY_REQUESTS);
        }
        if (app.getState() == State.STOP) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.TRADE_LOCKED.toString()), HttpStatus.LOCKED);
        }
        if (form.getUsername() == null)
            return new ResponseEntity<>(new RemoveAllResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        String message = orderService.removeAll(form.getUsername());
        return new ResponseEntity<>(new RemoveAllResponse(message), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/remove")
    public ResponseEntity<RemoveAllResponse> remove(@Valid @RequestBody RemoveRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimit(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.RATE_LIMITED.toString()), HttpStatus.TOO_MANY_REQUESTS);
        }
        if (app.getState() == State.STOP) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.TRADE_LOCKED.toString()), HttpStatus.LOCKED);
        }
        if (form.getUsername() == null)
            return new ResponseEntity<>(new RemoveAllResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        String message = orderService.removeOrder(form.getUsername(), form.getOrderID());
        return new ResponseEntity<>(new RemoveAllResponse(message), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/bot_remove")
    public ResponseEntity<RemoveAllResponse> botRemove(@Valid @RequestBody RemoveRequest form) {
        if (!botAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        }
        if (app.getState() == State.STOP) {
            return new ResponseEntity<>(new RemoveAllResponse(Message.TRADE_LOCKED.toString()), HttpStatus.LOCKED);
        }
        if (form.getUsername() == null)
            return new ResponseEntity<>(new RemoveAllResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        String message = orderService.removeOrder(form.getUsername(), form.getOrderID());
        return new ResponseEntity<>(new RemoveAllResponse(message), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/market_order")
    public ResponseEntity<MarketOrderResponse> marketOrderResponse(@Valid @RequestBody MarketOrderRequest form) {
        if (!privatePageAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new MarketOrderResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        }
        if (!rateLimit(form)) {
            return new ResponseEntity<>(new MarketOrderResponse(Message.RATE_LIMITED.toString()), HttpStatus.TOO_MANY_REQUESTS);
        }
        if (app.getState() == State.STOP) {
            return new ResponseEntity<>(new MarketOrderResponse(Message.TRADE_LOCKED.toString()), HttpStatus.LOCKED);
        }
        String message = orderService.placeMarketOrder(form.getUsername(), form.getTicker(), form.getVolume(), form.getBid());
        return new ResponseEntity<>(new MarketOrderResponse(message), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/bot_market_order")
    public ResponseEntity<MarketOrderResponse> botMarketOrderResponse(@Valid @RequestBody BotMarketOrderRequest form) {
        if (!botAuthenticator.authenticate(form)) {
            return new ResponseEntity<>(new MarketOrderResponse(Message.AUTHENTICATION_FAILED.toString()), HttpStatus.UNAUTHORIZED);
        }
        if (app.getState() == State.STOP) {
            return new ResponseEntity<>(new MarketOrderResponse(Message.TRADE_LOCKED.toString()), HttpStatus.LOCKED);
        }
        String message = orderService.placeMarketOrder(form.getUsername(), form.getTicker(), form.getVolume(), form.getBid());
        return new ResponseEntity<>(new MarketOrderResponse(message), HttpStatus.OK);
    }
}
