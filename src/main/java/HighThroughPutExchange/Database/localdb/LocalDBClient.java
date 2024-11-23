package HighThroughPutExchange.Database.localdb;

import HighThroughPutExchange.Database.abstractions.AbstractDBClient;
import HighThroughPutExchange.Database.abstractions.AbstractDBTable;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.Database.exceptions.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class LocalDBClient extends AbstractDBClient {

    private File file;
    private ObjectMapper objectMapper;
    private HashMap<String, LocalDBTable> tables;

    public LocalDBClient(String path) {
        file = new File(path);
        objectMapper = new ObjectMapper();
        try {
            tables = objectMapper.readerFor(HashMap.class).readValue(file);
        } catch (IOException e) {
            tables = new HashMap<>();
        }
    }

    @Override
    public AbstractDBTable createTable(String tableName) throws AlreadyExistsException {
        if (tables.containsKey(tableName)) {throw new AlreadyExistsException();}
        LocalDBTable output = new LocalDBTable(tableName);
        tables.put(tableName, output);
        return output;
    }

    @Override
    public AbstractDBTable getTable(String tableName) throws NotFoundException {
        if (!tables.containsKey(tableName)) {throw new NotFoundException();}
        return tables.get(tableName);
    }

    @Override
    public void destroyTable(String tableName) throws NotFoundException {
        if (!tables.containsKey(tableName)) {throw new NotFoundException();}
        tables.remove(tableName);
    }

    @Override
    public void closeClient() throws Exception {
        ObjectWriter writer = objectMapper.writer().withDefaultPrettyPrinter();
        try {
            writer.writeValue(file, tables);
        } catch (IOException e) {
            throw e;
        }
    }
}
