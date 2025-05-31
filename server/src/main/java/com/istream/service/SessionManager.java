package com.istream.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.istream.database.DatabaseManager;
import com.istream.database.dao.UserDAO;
import com.istream.model.Session;

public class SessionManager {
    private static final long SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(30);
    private final DatabaseManager dbManager;
    private final UserDAO userDAO;
    private final ConcurrentHashMap<String, Session> activeSessions;

    public SessionManager() {
        this.dbManager = new DatabaseManager();
        this.userDAO = dbManager.getUserDAO();
        this.activeSessions = new ConcurrentHashMap<>();
        startSessionCleanup();
    }

    public String createSession(int userId) throws SQLException {
        String token = UUID.randomUUID().toString();
        Session session = new Session(token, userId, 30); // 30 minutes validity

        String sql = "INSERT INTO sessions (token, user_id, expires_at) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            pstmt.setInt(2, userId);
            pstmt.setTimestamp(3, Timestamp.valueOf(session.getExpiryTime()));
            pstmt.executeUpdate();
        }

        activeSessions.put(token, session);
        return token;
    }

    public boolean validateSession(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // Check in-memory cache first
        Session session = activeSessions.get(token);
        if (session != null) {
            if (session.isValid()) {
                return true;
            }
            activeSessions.remove(token);
            return false;
        }

        // If not in cache, check database
        try {
            String sql = "SELECT * FROM sessions WHERE token = ? AND expires_at > NOW()";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, token);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    Session newSession = new Session(token, userId, 30);
                    activeSessions.put(token, newSession);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void invalidateSession(String token) {
        activeSessions.remove(token);
        try {
            String sql = "DELETE FROM sessions WHERE token = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, token);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Integer getUserIdFromToken(String token) {
        if (!validateSession(token)) {
            return null;
        }

        Session session = activeSessions.get(token);
        if (session != null) {
            return session.getUserId();
        }

        try {
            String sql = "SELECT user_id FROM sessions WHERE token = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, token);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startSessionCleanup() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    cleanupExpiredSessions();
                    Thread.sleep(TimeUnit.MINUTES.toMillis(5)); // Run every 5 minutes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    private void cleanupExpiredSessions() {
        // Cleanup in-memory sessions
        activeSessions.entrySet().removeIf(entry -> !entry.getValue().isValid());

        // Cleanup database sessions
        try {
            String sql = "DELETE FROM sessions WHERE expires_at < NOW()";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}