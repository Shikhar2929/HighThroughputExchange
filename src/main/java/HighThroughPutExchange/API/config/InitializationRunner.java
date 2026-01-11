package HighThroughPutExchange.API.config;

import HighThroughPutExchange.API.database_objects.User;
import HighThroughPutExchange.Database.localdb.LocalDBTable;
import HighThroughPutExchange.MatchingEngine.MatchingEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitializationRunner implements ApplicationRunner {

    private final MatchingEngine matchingEngine;
    private final LocalDBTable<User> users;
    private final LocalDBTable<User> bots;

    public InitializationRunner(MatchingEngine matchingEngine, @Qualifier("usersTable") LocalDBTable<User> users,
            @Qualifier("botsTable") LocalDBTable<User> bots) {
        this.matchingEngine = matchingEngine;
        this.users = users;
        this.bots = bots;
    }

    @Override
    public void run(ApplicationArguments args) {
        Iterable<String> userKeys = this.users.getAllKeys();
        for (String user : userKeys) {
            matchingEngine.initializeUser(user);
        }
        Iterable<String> botKeys = this.bots.getAllKeys();
        for (String bot : botKeys) {
            matchingEngine.initializeBot(bot);
        }
    }
}
