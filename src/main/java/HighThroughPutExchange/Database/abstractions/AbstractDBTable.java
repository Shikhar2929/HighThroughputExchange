package HighThroughPutExchange.Database.abstractions;

import HighThroughPutExchange.Database.entry.DBEntry;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;

public abstract class AbstractDBTable<T extends DBEntry> {
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract void putItem(T item) throws AlreadyExistsException;
    public abstract boolean containsItem(String key);
    public abstract T getItem(String key);
    public abstract void deleteItem(String key);
}
