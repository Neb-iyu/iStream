package com.istream.client.util;

import com.istream.client.service.RMIClient;
import com.istream.client.service.RMIClientImpl;
import com.istream.model.User;

public class SessionHolder {
    private final RMIClient rmiClient;
    private String authToken;
    private String username;
    
    public SessionHolder(RMIClient rmiClient) {
        this.rmiClient = rmiClient;
    }
    
    public void createSession(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
        try {
            User user = rmiClient.getCurrentUser();
            if (user != null && rmiClient instanceof RMIClientImpl) {
                ((RMIClientImpl) rmiClient).setUserId(user.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void clearSession() {
        try {
            if (authToken != null) {
                rmiClient.logout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.authToken = null;
        this.username = null;
        if (rmiClient instanceof RMIClientImpl) {
            ((RMIClientImpl) rmiClient).setUserId(-1);
        }
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public String getUsername() {
        return username;
    }
    
    public int getCurrentUserId() {
        if (rmiClient instanceof RMIClientImpl) {
            return ((RMIClientImpl) rmiClient).getUserId();
        }
        return -1;
    }
    
    public boolean isLoggedIn() {
        try {
            return authToken != null && rmiClient.isLoggedIn();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}