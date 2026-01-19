package HighThroughPutExchange.api.config;

import HighThroughPutExchange.api.entities.Session;
import HighThroughPutExchange.api.entities.User;
import HighThroughPutExchange.database.entry.DBEntry;
import HighThroughPutExchange.database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.database.exceptions.NotFoundException;
import HighThroughPutExchange.database.localdb.LocalDBClient;
import HighThroughPutExchange.database.localdb.LocalDBTable;
import java.util.HashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalDBConfig {

    @Bean
    public LocalDBClient localDBClient() {
        HashMap<String, Class<? extends DBEntry>> mapping = new HashMap<>();
        mapping.put("users", User.class);
        mapping.put("sessions", Session.class);
        mapping.put("bots", User.class);
        mapping.put("botSessions", Session.class);
        return new LocalDBClient("data.json", mapping);
    }

    private <T extends DBEntry> LocalDBTable<T> ensureTable(LocalDBClient client, String name) {
        try {
            return (LocalDBTable<T>) client.getTable(name);
        } catch (NotFoundException e) {
            try {
                client.createTable(name);
                return (LocalDBTable<T>) client.getTable(name);
            } catch (AlreadyExistsException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Bean
    public LocalDBTable<User> usersTable(LocalDBClient client) {
        return ensureTable(client, "users");
    }

    @Bean
    public LocalDBTable<User> botsTable(LocalDBClient client) {
        return ensureTable(client, "bots");
    }

    @Bean
    public LocalDBTable<Session> sessionsTable(LocalDBClient client) {
        return ensureTable(client, "sessions");
    }

    @Bean
    public LocalDBTable<Session> botSessionsTable(LocalDBClient client) {
        return ensureTable(client, "botSessions");
    }
}
