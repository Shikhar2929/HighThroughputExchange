package hte.api.config;

import hte.api.entities.Session;
import hte.api.entities.User;
import hte.database.entry.DBEntry;
import hte.database.exceptions.AlreadyExistsException;
import hte.database.exceptions.NotFoundException;
import hte.database.localdb.LocalDBClient;
import hte.database.localdb.LocalDBTable;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalDBConfig {
    @Bean
    LocalDBClient localDBClient(@Value("${hte.db.path:assets/data.json}") String dbPath) {
        HashMap<String, Class<? extends DBEntry>> mapping = new HashMap<>();
        mapping.put("users", User.class);
        mapping.put("sessions", Session.class);
        mapping.put("bots", User.class);
        mapping.put("botSessions", Session.class);
        return new LocalDBClient(dbPath, mapping);
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
    LocalDBTable<User> usersTable(LocalDBClient client) {
        return ensureTable(client, "users");
    }

    @Bean
    LocalDBTable<User> botsTable(LocalDBClient client) {
        return ensureTable(client, "bots");
    }

    @Bean
    LocalDBTable<Session> sessionsTable(LocalDBClient client) {
        return ensureTable(client, "sessions");
    }

    @Bean
    LocalDBTable<Session> botSessionsTable(LocalDBClient client) {
        return ensureTable(client, "botSessions");
    }
}
