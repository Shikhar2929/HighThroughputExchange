package hte.api.entities;

import hte.database.entry.DBEntry;

public class User extends DBEntry {

    private String username;
    private String name;
    private String apiKey;
    private String apiKey2;
    private String email;

    public User() {}

    public User(String username, String name, String apiKey, String email) {
        this(username, name, apiKey, apiKey, email);
    }

    public String getApiKey2() {
        return apiKey2;
    }

    public void setApiKey2(String apiKey2) {
        this.apiKey2 = apiKey2;
    }

    public User(String username, String name, String apiKey, String apiKey2, String email) {
        this.username = username;
        this.name = name;
        this.apiKey = apiKey;
        this.apiKey2 = apiKey2;
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
