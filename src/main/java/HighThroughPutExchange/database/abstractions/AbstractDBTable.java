package HighThroughPutExchange.database.abstractions;

import HighThroughPutExchange.database.entry.DBEntry;
import HighThroughPutExchange.database.exceptions.AlreadyExistsException;

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
