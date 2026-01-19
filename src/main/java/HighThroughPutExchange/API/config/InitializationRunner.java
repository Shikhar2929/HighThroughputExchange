package HighThroughPutExchange.api.config;

import HighThroughPutExchange.api.repository.BotsRepository;
import HighThroughPutExchange.api.repository.UsersRepository;
import HighThroughPutExchange.matchingengine.MatchingEngine;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitializationRunner implements ApplicationRunner {

    private final MatchingEngine matchingEngine;
    private final UsersRepository users;
    private final BotsRepository bots;

    public InitializationRunner(
            MatchingEngine matchingEngine, UsersRepository users, BotsRepository bots) {
        this.matchingEngine = matchingEngine;
        this.users = users;
        this.bots = bots;
    }

    @Override
    public void run(ApplicationArguments args) {
        Iterable<String> userKeys = this.users.keys();
        for (String user : userKeys) {
            matchingEngine.initializeUser(user);
        }
        Iterable<String> botKeys = this.bots.keys();
        for (String bot : botKeys) {
            matchingEngine.initializeBot(bot);
        }
    }
}
