package HighThroughPutExchange.API.repository;

public interface KeyEnumerable<K> {
    Iterable<K> keys();
}
