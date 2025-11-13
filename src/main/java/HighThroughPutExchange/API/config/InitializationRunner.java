package HighThroughPutExchange.API.config;

import HighThroughPutExchange.API.authentication.BotAuthenticator;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import HighThroughPutExchange.API.database_objects.Session;
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
    private final LocalDBTable<Session> sessions;
    private final LocalDBTable<Session> botSessions;

    public InitializationRunner(MatchingEngine matchingEngine, @Qualifier("usersTable") LocalDBTable<User> users,
            @Qualifier("botsTable") LocalDBTable<User> bots, @Qualifier("sessionsTable") LocalDBTable<Session> sessions,
            @Qualifier("botSessionsTable") LocalDBTable<Session> botSessions) {
        this.matchingEngine = matchingEngine;
        this.users = users;
        this.bots = bots;
        this.sessions = sessions;
        this.botSessions = botSessions;
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

        PrivatePageAuthenticator.buildInstance(this.sessions);
        BotAuthenticator.buildInstance(this.botSessions);
    }
}
