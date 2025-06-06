package com.istream.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import com.istream.database.DatabaseManager;
import com.istream.database.dao.UserDAO;

public class SessionManager {
    private static final long SESSION_DURATION = 30 * 60 * 1000; // 30 minutes in milliseconds
    private final DatabaseManager dbManager;
    private final UserDAO userDAO;

    public SessionManager() {
        this.dbManager = new DatabaseManager();
        this.userDAO = dbManager.getUserDAO();
    }

    public SessionManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.userDAO = dbManager.getUserDAO();
    }

    public String createSession(int userId) {
        String token = generateToken();
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + SESSION_DURATION);

        String sql = "INSERT INTO sessions (token, user_id, expires_at) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            pstmt.setInt(2, userId);
            pstmt.setTimestamp(3, expiresAt);
            pstmt.executeUpdate();
            
            return token;
        } catch (SQLException e) {
            System.err.println("Error creating session: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean validateSession(String token) {
        if (token == null) return false;

        String sql = "SELECT * FROM sessions WHERE token = ? AND expires_at > NOW()";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Update the session expiry time
                    updateSessionExpiry(token);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating session: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public void invalidateSession(String token) {
        String sql = "DELETE FROM sessions WHERE token = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error invalidating session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Integer getUserIdFromToken(String token) {
        if (token == null) return null;

        String sql = "SELECT user_id FROM sessions WHERE token = ? AND expires_at > NOW()";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user ID from token: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void updateSessionExpiry(String token) {
        String sql = "UPDATE sessions SET expires_at = ? WHERE token = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Timestamp newExpiry = new Timestamp(System.currentTimeMillis() + SESSION_DURATION);
            pstmt.setTimestamp(1, newExpiry);
            pstmt.setString(2, token);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating session expiry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void cleanupExpiredSessions() {
        String sql = "DELETE FROM sessions WHERE expires_at <= NOW()";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error cleaning up expired sessions: " + e.getMessage());
            e.printStackTrace();
        }
    }
}