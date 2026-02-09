package hte.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "hte")
@RestController
@EnableScheduling
public class ServerApplication {
    private State state = State.STOP;

    public State getState() {
        return state;
    }

    public void setStateInternal(State newState) {
        this.state = newState;
    }

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return new ResponseEntity<>("HTE API Server is running.", HttpStatus.OK);
    }
}
