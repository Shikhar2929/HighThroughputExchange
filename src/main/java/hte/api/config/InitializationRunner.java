package hte.api.config;

import hte.api.repository.BotsRepository;
import hte.api.repository.UsersRepository;
import hte.matchingengine.MatchingEngine;
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
