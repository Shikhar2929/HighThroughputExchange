package HighThroughPutExchange.API.controller;

import HighThroughPutExchange.API.api_objects.responses.GetVersionResponse;
import HighThroughPutExchange.Common.UpdateIdGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateController {
    private UpdateIdGenerator updateIdGenerator;

    public UpdateController(UpdateIdGenerator updateIdGenerator) {
        this.updateIdGenerator = updateIdGenerator;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/version")
    public ResponseEntity<GetVersionResponse> getVersion() {
        return new ResponseEntity<>(new GetVersionResponse(updateIdGenerator.get()), HttpStatus.OK);
    }
}
