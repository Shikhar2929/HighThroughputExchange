package hte.database.localdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import hte.database.abstractions.AbstractDBClient;
import hte.database.entry.DBEntry;
import hte.database.exceptions.AlreadyExistsException;
import hte.database.exceptions.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class LocalDBClient extends AbstractDBClient {

    private File file;
    private ObjectMapper objectMapper;
    private ConcurrentHashMap<String, LocalDBTable> tables;

    public LocalDBClient(String path, HashMap<String, Class<? extends DBEntry>> tableMapping) {
        file = new File(path);
        objectMapper = new ObjectMapper();
        tables = new ConcurrentHashMap<>();
        HashMap<String, LinkedHashMap<String, LinkedHashMap<String, Object>>> temp;
        try {
            temp = objectMapper.readerFor(HashMap.class).readValue(file);
            LocalDBTable table;
            for (String tableName : temp.keySet()) {
                table = new LocalDBTable<>(tableName);
                for (String key : temp.get(tableName).get("backing").keySet()) {
                    try {
                        table.putItem(
                                objectMapper.convertValue(
                                        temp.get(tableName).get("backing").get(key),
                                        tableMapping.getOrDefault(tableName, null)));
                    } catch (AlreadyExistsException e) {
                        throw new RuntimeException(e);
                    }
                }
                tables.put(tableName, table);
            }
        } catch (IOException e) {
            tables = new ConcurrentHashMap<>();
        }
    }

    @Override
    public LocalDBTable<DBEntry> createTable(String tableName) throws AlreadyExistsException {
        if (tables.containsKey(tableName)) {
            throw new AlreadyExistsException();
        }
        LocalDBTable<DBEntry> output = new LocalDBTable<DBEntry>(tableName);
        tables.put(tableName, output);
        return output;
    }

    @Override
    public LocalDBTable getTable(String tableName) throws NotFoundException {
        if (!tables.containsKey(tableName)) {
            throw new NotFoundException();
        }
        return tables.get(tableName);
    }

    @Override
    public void destroyTable(String tableName) throws NotFoundException {
        if (!tables.containsKey(tableName)) {
            throw new NotFoundException();
        }
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
