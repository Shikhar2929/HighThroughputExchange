package HighThroughPutExchange.API.controller;

import HighThroughPutExchange.API.ServerApplication;
import HighThroughPutExchange.API.State;
import HighThroughPutExchange.API.api_objects.Operations.Operation;
import HighThroughPutExchange.API.api_objects.requests.BatchRequest;
import HighThroughPutExchange.API.api_objects.responses.BatchResponse;
import HighThroughPutExchange.API.api_objects.responses.OperationResponse;
import HighThroughPutExchange.API.service.AuthService;
import HighThroughPutExchange.API.service.BatchService;
import HighThroughPutExchange.Common.Message;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BatchController {

    private final BatchService batchService;
    private final AuthService authService;
    private final ServerApplication app;

    public BatchController(
            BatchService batchService, AuthService authService, ServerApplication app) {
        this.batchService = batchService;
        this.authService = authService;
        this.app = app;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/batch")
    public ResponseEntity<BatchResponse> processBatch(@Valid @RequestBody BatchRequest form) {
        if (!authService.authenticateBot(form)) {
            return new ResponseEntity<>(
                    new BatchResponse(Message.AUTHENTICATION_FAILED.toString(), null),
                    HttpStatus.UNAUTHORIZED);
        }
        if (app.getState() == State.STOP) {
            return new ResponseEntity<>(
                    new BatchResponse(Message.TRADE_LOCKED.toString(), null), HttpStatus.LOCKED);
        }

        if (form.getOperations().size() > batchService.getMaxOperations()) {
            return new ResponseEntity<>(
                    new BatchResponse("EXCEEDED_OPERATION_LIMIT", null), HttpStatus.BAD_REQUEST);
        }

        List<Operation> ops = form.getOperations();
        List<OperationResponse> responses = batchService.processBatch(form.getUsername(), ops);

        if (responses == null) {
            return new ResponseEntity<>(
                    new BatchResponse("UNKNOWN OPERATION", null), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new BatchResponse("SUCCESS", responses), HttpStatus.OK);
    }
}
