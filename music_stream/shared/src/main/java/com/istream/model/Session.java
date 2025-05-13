package com.istream.model;


import java.io.Serializable;
import java.time.LocalDateTime;

public class Session implements Serializable {
    private final String token;
    private final int userId;
    private final LocalDateTime expiryTime;
    
    public Session(String token, int userId, int validityMinutes) {
        this.token = token;
        this.userId = userId;
        this.expiryTime = LocalDateTime.now().plusMinutes(validityMinutes);
    }
    
    // Getters
    public boolean isValid() {
        return LocalDateTime.now().isBefore(expiryTime);
    }
    public String getToken() {
        return token;
    }
    public int getUserId() {
        return userId;
    }
    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }
}