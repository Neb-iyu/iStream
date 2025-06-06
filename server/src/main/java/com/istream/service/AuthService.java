package com.istream.service;

import java.sql.SQLException;
import com.istream.database.DatabaseManager;
import com.istream.database.dao.UserDAO;
import com.istream.model.User;
import com.istream.util.Security;
import com.istream.model.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AuthService {
    private static final String DEFAULT_PROFILE_PICTURE_PATH = "/resources/images/default-profile.png";
    private static final long SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(30);

    private final DatabaseManager dbManager;
    private final UserDAO userDAO;
    private final SessionManager sessionManager;
    private final ConcurrentHashMap<String, Long> activeSessions;

    public AuthService() {
        this.dbManager = new DatabaseManager();
        this.userDAO = dbManager.getUserDAO();
        this.sessionManager = new SessionManager();
        this.activeSessions = new ConcurrentHashMap<>();
    }

    public AuthService(DatabaseManager dbManager, SessionManager sessionManager) {
        this.dbManager = dbManager;
        this.userDAO = dbManager.getUserDAO();
        this.sessionManager = sessionManager;
        this.activeSessions = new ConcurrentHashMap<>();
    }

    public String login(String username, String password) throws SQLException {
        try {
            System.out.println("Login attempt for user: " + username);
            User user = userDAO.getUserByUsername(username);
            if (user != null && Security.verifyPassword(password, user.getPassword())) {
                String sessionToken = sessionManager.createSession(user.getId());
                if (sessionToken != null) {
                    System.out.println("Login successful for user: " + username);
                    activeSessions.put(sessionToken, System.currentTimeMillis());
                }
                return sessionToken;
            } else {
                System.err.println("Login failed for user: " + username + " - Invalid credentials");
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Unexpected error during login: " + e.getMessage(), e);
        }
    }

    public void logout(String authToken) {
        activeSessions.remove(authToken);
        sessionManager.invalidateSession(authToken);
    }

    public boolean isLoggedIn(String authToken) {
        if (activeSessions.containsKey(authToken)) {
            activeSessions.put(authToken, System.currentTimeMillis()); // Update last access time
            return true;
        }
        return false;
    }

    public String register(String username, String password, String email) throws SQLException {
        try {
            System.out.println("Registration attempt for user: " + username);
            if (userDAO.getUserByUsername(username) != null) {
                return null; // Username already exists
            }

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(Security.hashPassword(password));
            newUser.setEmail(email);
            newUser.setProfilePicture(DEFAULT_PROFILE_PICTURE_PATH);

            int userId = userDAO.createUser(newUser);
            String sessionToken = sessionManager.createSession(userId);
            System.out.println("Registration successful for user: " + username);
            return sessionToken;
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Unexpected error during registration: " + e.getMessage(), e);
        }
    }

    public boolean validateSession(String token) {
        return sessionManager.validateSession(token);
    }

    public Integer getUserIdFromToken(String token) {
        return sessionManager.getUserIdFromToken(token);
    }

    public void cleanupOldSessions() {
        long currentTime = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > SESSION_TIMEOUT
        );
    }
}
