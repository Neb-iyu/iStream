package com.istream.client.util;

import com.istream.rmi.MusicService;
import java.rmi.RemoteException;

public class SessionHolder {
    private final MusicService musicService;
    private String authToken;
    private String username;
    
    public SessionHolder(MusicService musicService) {
        this.musicService = musicService;
    }
    
    public void createSession(String token, String username) {
        this.authToken = token;
        this.username = username;
    }
    public boolean login(String username, String password) {
        try {
            this.authToken = musicService.login(username, password);
            return authToken != null;
        } catch (RemoteException e) {
            return false;
        }
    }
    
    public void logout() {
        try {
            musicService.logout(authToken);
        } catch (RemoteException e) {
            // Log error
        } finally {
            authToken = null;
        }
    }
    
    public boolean isLoggedIn() {
        return authToken != null;
    }
    
    // Other session-aware methods...
    public String getCurrentSessionToken() {
        return authToken;
    }

    public String getCurrentUsername() {
        return username;
    }

    // FIXME: This needs a proper implementation to get the user ID.
    // This might involve calling a method on musicService to get user details by username/token,
    // or the login method should return the userId along with the token.
    public int getCurrentUserId() {
        if (username != null && !username.isEmpty()) {
            // Placeholder: try to derive from username, e.g. "user1" -> 1. Highly unreliable.
            // Or, if you have a fixed mapping or a test user ID.
            // For demonstration, returning a hash code or a fixed value.
            // return Math.abs(username.hashCode() % 1000); // Example of a non-stable ID
            if ("testuser".equals(username)) return 1; // Example for a known user
            return 1; // Default placeholder, replace with actual logic
        }
        return -1; // Indicates no user or ID not found
    }
    
}