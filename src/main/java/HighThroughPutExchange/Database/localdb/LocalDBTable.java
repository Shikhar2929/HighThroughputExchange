package HighThroughPutExchange.Database.localdb;

import HighThroughPutExchange.Database.abstractions.AbstractDBTable;
import HighThroughPutExchange.Database.entry.DBEntry;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.Database.exceptions.NotFoundException;

import java.util.HashMap;

public class LocalDBTable extends AbstractDBTable {

    private HashMap<String, DBEntry> backing;

    public LocalDBTable(String name) {
        this.name = name;
        backing = new HashMap<>();
    }

    @Override
    public void putItem(DBEntry item) throws AlreadyExistsException {
        // todo: ask about error handling
        if (item == null) {return;}
        if (backing.containsKey(item.getHash())) {throw new AlreadyExistsException();}
        backing.put(item.getHash(), item);
    }

    @Override
    public boolean containsItem(String key) {
        return backing.containsKey(key);
    }

    @Override
    public DBEntry getItem(String key) {
        return backing.get(key);
    }

    @Override
    public void deleteItem(String key) {
        backing.remove(key);
    }
}
