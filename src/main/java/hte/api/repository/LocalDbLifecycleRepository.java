package hte.api.repository;

import hte.database.localdb.LocalDBClient;
import org.springframework.stereotype.Repository;

@Repository
public class LocalDbLifecycleRepository implements DbLifecycleRepository {

    private final LocalDBClient client;

    public LocalDbLifecycleRepository(LocalDBClient client) {
        this.client = client;
    }

    @Override
    public void close() throws Exception {
        client.closeClient();
    }
}
