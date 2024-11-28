package HighThroughPutExchange.Database.localdb;

import HighThroughPutExchange.Database.abstractions.AbstractDBClient;
import HighThroughPutExchange.Database.entry.DBEntry;
import HighThroughPutExchange.Database.exceptions.AlreadyExistsException;
import HighThroughPutExchange.Database.exceptions.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class LocalDBClient extends AbstractDBClient {

    private File file;
    private ObjectMapper objectMapper;
    private HashMap<String, LocalDBTable> tables;

    public LocalDBClient(String path, HashMap<String, Class<? extends DBEntry>> tableMapping) {
        file = new File(path);
        objectMapper = new ObjectMapper();
        tables = new HashMap<>();
        HashMap<String, LinkedHashMap<String, LinkedHashMap<String, Object>>> temp;
        try {
            temp = objectMapper.readerFor(HashMap.class).readValue(file);
            LocalDBTable table;
            for (String tableName: temp.keySet()) {
                table = new LocalDBTable<>(tableName, tableMapping.get(tableName));
                for (String key: temp.get(tableName).get("backing").keySet()) {
                    System.out.println(temp.get(tableName).get("backing").get(key).getClass());
                    try {
                        table.putItem(
                                objectMapper.convertValue(
                                        temp.get(tableName).get("backing").get(key),
                                        tableMapping.get(tableName)
                                )
                        );
                    } catch (Exception e) {System.out.println(e); System.out.println(key);}
                }
                tables.put(tableName, table);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public LocalDBTable<DBEntry> createTable(String tableName, Class<? extends DBEntry> c) throws AlreadyExistsException {
        if (tables.containsKey(tableName)) {throw new AlreadyExistsException();}
        LocalDBTable<DBEntry> output = new LocalDBTable(tableName, c);
        tables.put(tableName, output);
        return output;
    }

    @Override
    public LocalDBTable getTable(String tableName) throws NotFoundException {
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
