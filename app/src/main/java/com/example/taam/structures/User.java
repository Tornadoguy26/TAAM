package com.example.taam.structures;

public class User {
    private String username;
    private String passhash;

    // Necessary for firebase conversion
    public User() {}

    public User(String username, String passhash) {
        this.username = username;
        this.passhash = passhash;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasshash() { return passhash; }
    public void setPasshash(String passhash) { this.passhash = passhash; }

}
