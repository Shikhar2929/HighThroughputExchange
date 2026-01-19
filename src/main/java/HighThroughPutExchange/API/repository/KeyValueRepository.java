package HighThroughPutExchange.API.repository;

import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;

public interface KeyValueRepository<K, V> {
    boolean exists(K key);

    V get(K key);

    void add(V value) throws AlreadyExistsException;
}
