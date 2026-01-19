package hte.api.repository;

public interface DbLifecycleRepository {
    void close() throws Exception;
}
