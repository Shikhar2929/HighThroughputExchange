package HighThroughPutExchange.API;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "HighThroughPutExchange")
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

    // public static void main(String[] args) {
    // SpringApplication.run(ServerApplication.class, args);
    // }
}
