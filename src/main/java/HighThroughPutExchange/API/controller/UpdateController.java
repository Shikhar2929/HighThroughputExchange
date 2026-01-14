package HighThroughPutExchange.API.controller;

import HighThroughPutExchange.API.api_objects.responses.GetVersionResponse;
import HighThroughPutExchange.API.api_objects.responses.GetUpdatesResponse;
import HighThroughPutExchange.API.api_objects.responses.SnapshotResponse;
import HighThroughPutExchange.Common.OrderbookUpdate;
import HighThroughPutExchange.Common.OrderbookUpdateLog;
import HighThroughPutExchange.Common.UpdateIdGenerator;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateController {
    private final UpdateIdGenerator updateIdGenerator;
    private final MatchingEngine matchingEngine;
    private final OrderbookUpdateLog orderbookUpdateLog;

    public UpdateController(UpdateIdGenerator updateIdGenerator, MatchingEngine matchingEngine, OrderbookUpdateLog orderbookUpdateLog) {
        this.updateIdGenerator = updateIdGenerator;
        this.matchingEngine = matchingEngine;
        this.orderbookUpdateLog = orderbookUpdateLog;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/version")
    public ResponseEntity<GetVersionResponse> getVersion() {
        return new ResponseEntity<>(new GetVersionResponse(updateIdGenerator.get()), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/updates")
    public ResponseEntity<GetUpdatesResponse> getUpdates(@RequestParam(name = "from") long from) {
        List<OrderbookUpdate> updates = orderbookUpdateLog.get(from);
        return new ResponseEntity<>(new GetUpdatesResponse(from, updateIdGenerator.get(), updates), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/snapshot")
    public ResponseEntity<SnapshotResponse> snapshot() {
        String snapshot = matchingEngine.serializeOrderBooks();
        return new ResponseEntity<>(new SnapshotResponse(snapshot, updateIdGenerator.get()), HttpStatus.OK);
    }
}
