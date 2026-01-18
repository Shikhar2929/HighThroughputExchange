package HighThroughPutExchange.API.repository;

import HighThroughPutExchange.API.database_objects.User;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;

public interface UsersRepository {
    boolean exists(String username);

    User get(String username);

    void add(User user) throws AlreadyExistsException;

    Iterable<String> keys();
}
