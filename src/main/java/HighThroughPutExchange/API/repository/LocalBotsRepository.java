package HighThroughPutExchange.api.repository;

import HighThroughPutExchange.api.entities.User;
import HighThroughPutExchange.database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.database.localdb.LocalDBTable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class LocalBotsRepository implements BotsRepository {

    private final LocalDBTable<User> bots;

    public LocalBotsRepository(@Qualifier("botsTable") LocalDBTable<User> bots) {
        this.bots = bots;
    }

    @Override
    public boolean exists(String username) {
        return bots.containsItem(username);
    }

    @Override
    public User get(String username) {
        return bots.getItem(username);
    }

    @Override
    public void add(User user) throws AlreadyExistsException {
        bots.putItem(user);
    }

    @Override
    public Iterable<String> keys() {
        return bots.getAllKeys();
    }
}
