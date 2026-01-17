package HighThroughPutExchange.API.controller;

import HighThroughPutExchange.API.api_objects.responses.GetUpdatesResponse;
import HighThroughPutExchange.API.api_objects.responses.GetVersionResponse;
import HighThroughPutExchange.API.api_objects.responses.SnapshotResponse;
import HighThroughPutExchange.Common.OrderbookUpdate;
import HighThroughPutExchange.Common.OrderbookSeqLog;
import HighThroughPutExchange.Common.SeqGenerator;
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
    private final SeqGenerator seqGenerator;
    private final MatchingEngine matchingEngine;
    private final OrderbookSeqLog orderbookSeqLog;

    public UpdateController(SeqGenerator seqGenerator, MatchingEngine matchingEngine, OrderbookSeqLog orderbookSeqLog) {
        this.seqGenerator = seqGenerator;
        this.matchingEngine = matchingEngine;
        this.orderbookSeqLog = orderbookSeqLog;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/version")
    public ResponseEntity<GetVersionResponse> getVersion() {
        return new ResponseEntity<>(new GetVersionResponse(seqGenerator.get()), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/updates")
    public ResponseEntity<GetUpdatesResponse> getUpdates(@RequestParam(name = "from") long from) {
        List<OrderbookUpdate> updates = orderbookSeqLog.get(from);
        return new ResponseEntity<>(new GetUpdatesResponse(from, seqGenerator.get(), updates), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/snapshot")
    public ResponseEntity<SnapshotResponse> snapshot() {
        String snapshot = matchingEngine.serializeOrderBooks();
        return new ResponseEntity<>(new SnapshotResponse(snapshot, seqGenerator.get()), HttpStatus.OK);
    }
}
