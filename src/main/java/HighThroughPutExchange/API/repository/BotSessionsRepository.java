package HighThroughPutExchange.API.repository;

import HighThroughPutExchange.API.database_objects.Session;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;

public interface BotSessionsRepository {
    boolean exists(String username);

    Session get(String username);

    void add(Session session) throws AlreadyExistsException;

    void delete(String username);
}
