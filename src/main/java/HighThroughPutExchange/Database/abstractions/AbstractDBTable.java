package HighThroughPutExchange.Database.abstractions;

import HighThroughPutExchange.Database.entry.DBEntry;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.Database.exceptions.NotFoundException;

public abstract class AbstractDBTable {
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract void putItem(DBEntry item) throws AlreadyExistsException;
    public abstract boolean containsItem(String key);
    public abstract DBEntry getItem(String key);
    public abstract void deleteItem(String key);
}
