package HighThroughPutExchange.api.repository;

import HighThroughPutExchange.api.entities.Session;
import HighThroughPutExchange.database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.database.localdb.LocalDBTable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class LocalSessionsRepository implements SessionsRepository {

    private final LocalDBTable<Session> sessions;

    public LocalSessionsRepository(@Qualifier("sessionsTable") LocalDBTable<Session> sessions) {
        this.sessions = sessions;
    }

    @Override
    public boolean exists(String username) {
        return sessions.containsItem(username);
    }

    @Override
    public Session get(String username) {
        return sessions.getItem(username);
    }

    @Override
    public void add(Session session) throws AlreadyExistsException {
        sessions.putItem(session);
    }

    @Override
    public void delete(String username) {
        sessions.deleteItem(username);
    }
}
