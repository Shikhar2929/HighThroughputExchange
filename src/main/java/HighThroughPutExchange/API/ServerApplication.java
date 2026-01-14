package HighThroughPutExchange.API;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

/*
todo
    make all task queue tasks into completable futures
    fix exception handling of database classes
    make all DBs threadsafe
        Atomicity - not guaranteed, but expose backing and mutex
        Consistency - guaranteed
        Isolation - guaranteed
        Durability - guaranteed
 */

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
