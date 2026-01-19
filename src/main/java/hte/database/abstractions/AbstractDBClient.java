package hte.database.abstractions;

import hte.database.exceptions.AlreadyExistsException;
import hte.database.exceptions.NotFoundException;

public abstract class AbstractDBClient {
    public abstract AbstractDBTable createTable(String tableName) throws AlreadyExistsException;

    public abstract AbstractDBTable getTable(String tableName) throws NotFoundException;

    public abstract void destroyTable(String tableName) throws NotFoundException;

    public abstract void closeClient() throws Exception;
}
