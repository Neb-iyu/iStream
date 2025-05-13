package com.istream.util;

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
    
}