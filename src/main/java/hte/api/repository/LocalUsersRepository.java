package hte.api.repository;

import hte.api.entities.User;
import hte.database.exceptions.AlreadyExistsException;
import hte.database.localdb.LocalDBTable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class LocalUsersRepository implements UsersRepository {

    private final LocalDBTable<User> users;

    public LocalUsersRepository(@Qualifier("usersTable") LocalDBTable<User> users) {
        this.users = users;
    }

    @Override
    public boolean exists(String username) {
        return users.containsItem(username);
    }

    @Override
    public User get(String username) {
        return users.getItem(username);
    }

    @Override
    public void add(User user) throws AlreadyExistsException {
        users.putItem(user);
    }

    @Override
    public Iterable<String> keys() {
        return users.getAllKeys();
    }
}
