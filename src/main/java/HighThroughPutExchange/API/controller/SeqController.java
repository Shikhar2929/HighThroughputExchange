package HighThroughPutExchange.API.controller;

import HighThroughPutExchange.API.api_objects.responses.GetLatestSeqResponse;
import HighThroughPutExchange.API.api_objects.responses.GetUpdateResponse;
import HighThroughPutExchange.API.api_objects.responses.SnapshotResponse;
import HighThroughPutExchange.Common.Message;
import HighThroughPutExchange.Common.OrderbookSeqLog;
import HighThroughPutExchange.Common.OrderbookUpdate;
import HighThroughPutExchange.Common.SeqGenerator;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import java.util.Optional;
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

    public SeqController(
            SeqGenerator seqGenerator,
            MatchingEngine matchingEngine,
            OrderbookSeqLog orderbookSeqLog) {
        this.seqGenerator = seqGenerator;
        this.matchingEngine = matchingEngine;
        this.orderbookSeqLog = orderbookSeqLog;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/latestSeq")
    public ResponseEntity<GetLatestSeqResponse> getLatestSeq() {
        return new ResponseEntity<>(
                new GetLatestSeqResponse(Message.SUCCESS.toString(), seqGenerator.get()),
                HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/updates")
    public ResponseEntity<GetUpdateResponse> getUpdate(@RequestParam long seq) {
        Optional<OrderbookUpdate> update = orderbookSeqLog.getBySeq(seq);
        if (update.isEmpty()) {
            return new ResponseEntity<>(
                    new GetUpdateResponse(Message.INVALID_SEQ_NUM.toString()),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                new GetUpdateResponse(Message.SUCCESS.toString(), seq, update.get()),
                HttpStatus.OK);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/snapshot")
    public ResponseEntity<SnapshotResponse> snapshot() {
        long latestSeq = seqGenerator.get();
        String snapshot = matchingEngine.serializeOrderBooks();

        if (snapshot == null) {
            return new ResponseEntity<>(
                    new SnapshotResponse(Message.BAD_INPUT.toString()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(
                new SnapshotResponse(Message.SUCCESS.toString(), snapshot, latestSeq),
                HttpStatus.OK);
    }
}
