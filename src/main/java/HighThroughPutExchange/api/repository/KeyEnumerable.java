package HighThroughPutExchange.api.repository;

public interface KeyEnumerable<K> {
    Iterable<K> keys();
}
