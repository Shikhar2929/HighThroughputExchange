package HighThroughPutExchange.API.repository;

public interface DbLifecycleRepository {
    void close() throws Exception;
}
