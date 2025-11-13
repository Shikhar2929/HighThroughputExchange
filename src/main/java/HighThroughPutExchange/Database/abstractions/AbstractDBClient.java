package HighThroughPutExchange.Database.abstractions;

import HighThroughPutExchange.Database.entry.DBEntry;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.Database.exceptions.NotFoundException;

public abstract class AbstractDBClient<T extends DBEntry> {
    public abstract AbstractDBTable<T> createTable(String tableName) throws AlreadyExistsException;

    public abstract AbstractDBTable<T> getTable(String tableName) throws NotFoundException;

    public abstract void destroyTable(String tableName) throws NotFoundException;

    public abstract void closeClient() throws Exception;
}
