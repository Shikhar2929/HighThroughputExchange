package HighThroughPutExchange.API.controller;

import HighThroughPutExchange.API.api_objects.responses.GetUpdatesResponse;
import HighThroughPutExchange.API.api_objects.responses.GetVersionResponse;
import HighThroughPutExchange.API.api_objects.responses.SnapshotResponse;
import HighThroughPutExchange.Common.OrderbookSeqLog;
import HighThroughPutExchange.Common.OrderbookUpdate;
import HighThroughPutExchange.Common.SeqGenerator;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeqController {
    private final SeqGenerator seqGenerator;
    private final MatchingEngine matchingEngine;
    private final OrderbookSeqLog orderbookSeqLog;

    public SeqController(SeqGenerator seqGenerator, MatchingEngine matchingEngine, OrderbookSeqLog orderbookSeqLog) {
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
    public ResponseEntity<?> getUpdates(@RequestParam long fromExclusive) {
        Long minSeq = orderbookSeqLog.getMinSeq();
        if (minSeq != null) {
            long minFromExclusive = minSeq - 1;
            if (fromExclusive < minFromExclusive) {
                return new ResponseEntity<>(Map.of("error", "from-too-old", "fromExclusive", fromExclusive, "minAvailableSeq", minSeq,
                        "minFromExclusive", minFromExclusive, "latestSeq", seqGenerator.get()), HttpStatus.GONE);
            }
        }

        List<OrderbookUpdate> updates = orderbookSeqLog.get(fromExclusive);
        return new ResponseEntity<>(new GetUpdatesResponse(fromExclusive, seqGenerator.get(), updates), HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/snapshot")
    public ResponseEntity<?> snapshot() {
        long version = seqGenerator.get();
        String snapshot = matchingEngine.serializeOrderBooks();

        if (snapshot == null) {
            return new ResponseEntity<>(Map.of("error", "snapshot-serialization-failed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(new SnapshotResponse(snapshot, version), HttpStatus.OK);
    }
}
