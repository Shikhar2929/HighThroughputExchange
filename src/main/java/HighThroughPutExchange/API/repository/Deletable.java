package HighThroughPutExchange.api.repository;

public interface Deletable<K> {
    void delete(K key);
}
