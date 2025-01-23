package HighThroughPutExchange.Database.localdb;

import java.util.concurrent.ConcurrentHashMap;
import HighThroughPutExchange.Database.abstractions.AbstractDBTable;
import HighThroughPutExchange.Database.entry.DBEntry;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;

import java.util.HashMap;

public class LocalDBTable<T extends DBEntry> extends AbstractDBTable<T> {

    private ConcurrentHashMap<String, T> backing;

    public ConcurrentHashMap<String, T> getBacking() {
        return backing;
    }

    public LocalDBTable(String name) {
        this.name = name;
        backing = new ConcurrentHashMap<>();
    }

    @Override
    public void putItem(T item) throws AlreadyExistsException {
        // todo: ask about error handling
        if (item == null) {return;}
        if (backing.containsKey(item.hashOut())) {throw new AlreadyExistsException();}
        if (item.hashOut() == null) {throw new AlreadyExistsException();}
        backing.put(item.hashOut(), item);
    }

    @Override
    public boolean containsItem(String key) {
        return backing.containsKey(key);
    }

    @Override
    public T getItem(String key) {
        return backing.get(key);
    }

    @Override
    public void deleteItem(String key) {
        backing.remove(key);
    }
    public Iterable<String> getAllKeys() {
        return backing.keySet();
    }
}
