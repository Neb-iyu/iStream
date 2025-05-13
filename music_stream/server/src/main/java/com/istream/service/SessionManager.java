package com.istream.service;


import com.istream.model.Session;
import com.istream.util.TokenGenerator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final int SESSION_VALIDITY_MINUTES = 30;
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();
    
    public String createSession(int userId) {
        String token = TokenGenerator.generateSecureToken();
        Session session = new Session(token, userId, SESSION_VALIDITY_MINUTES);
        activeSessions.put(token, session);
        return token;
    }
    
    public boolean validateSession(String token) {
        Session session = activeSessions.get(token);
        return session != null && session.isValid();
    }
    
    public void invalidateSession(String token) {
        activeSessions.remove(token);
    }
    
    public int getUserId(String token) {
        Session session = activeSessions.get(token);
        return session != null ? session.getUserId() : -1;
    }
}