package HighThroughPutExchange.Database.abstractions;

import HighThroughPutExchange.Database.entry.DBEntry;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.Database.exceptions.NotFoundException;

public abstract class AbstractDBClient {
    public abstract AbstractDBTable createTable(String tableName, Class<? extends DBEntry> c) throws AlreadyExistsException;
    public abstract AbstractDBTable getTable(String tableName) throws NotFoundException;
    public abstract void destroyTable(String tableName) throws NotFoundException;
    public abstract void closeClient() throws Exception;
}
