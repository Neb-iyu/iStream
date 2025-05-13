package com.istream.model;

public class User {
    private int id;

    private String username;
    private String hashedPassword;
    private String email;
    private String profilePicturePath; // path to the user's profile picture
    
    public User(int id, String username, String hashedPassword, String email, String profilePicturePath) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.email = email;
        this.profilePicturePath = profilePicturePath;
    }
    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return hashedPassword;
    }
    public String getEmail() {
        return email;
    }
}
