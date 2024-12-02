package HighThroughPutExchange.API.database_objects;

import HighThroughPutExchange.Database.entry.DBEntry;

public class User extends DBEntry {

    private String username;
    private String name;
    private String apiKey;
    private String email;

    public User() {}

    public User(String username, String name, String apiKey, String email) {
        this.username = username;
        this.name = name;
        this.apiKey = apiKey;
        this.email = email;
    }


    @Override
    public String hashOut() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
