package HighThroughPutExchange.API.repository;

public interface Deletable<K> {
    void delete(K key);
}
