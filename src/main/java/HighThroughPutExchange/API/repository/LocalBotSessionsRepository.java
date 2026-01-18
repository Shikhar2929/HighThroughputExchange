package HighThroughPutExchange.API.repository;

import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.Database.localdb.LocalDBTable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class LocalBotSessionsRepository implements BotSessionsRepository {

    private final LocalDBTable<Session> botSessions;

    public LocalBotSessionsRepository(
            @Qualifier("botSessionsTable") LocalDBTable<Session> botSessions) {
        this.botSessions = botSessions;
    }

    @Override
    public boolean exists(String username) {
        return botSessions.containsItem(username);
    }

    @Override
    public Session get(String username) {
        return botSessions.getItem(username);
    }

    @Override
    public void add(Session session) throws AlreadyExistsException {
        botSessions.putItem(session);
    }

    @Override
    public void delete(String username) {
        botSessions.deleteItem(username);
    }
}
