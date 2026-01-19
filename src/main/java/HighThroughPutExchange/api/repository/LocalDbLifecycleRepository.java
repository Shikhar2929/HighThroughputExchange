package HighThroughPutExchange.api.repository;

import HighThroughPutExchange.database.localdb.LocalDBClient;
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
