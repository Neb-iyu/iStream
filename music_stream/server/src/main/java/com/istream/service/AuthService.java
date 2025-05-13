package com.istream.service;

import com.istream.database.DatabaseManager;
import com.istream.model.User;
import com.istream.util.Security;

import java.sql.SQLException;

import com.istream.model.Session;

public class AuthService {
    private static final String DEFAULT_PROFILE_PICTURE_PATH = "/resources/images/default-profile.png"; // Default profile picture path

    private final DatabaseManager dbManager;
    private final SessionManager sessionManager;

    public AuthService(DatabaseManager dbManager, SessionManager sessionManager) {
        this.dbManager = dbManager;
        this.sessionManager = sessionManager;
    }

    public String login(String username, String password) throws SQLException {
        User user = dbManager.getUserByUsername(username);
        if (user != null && Security.verifyPassword(password, user.getPassword())) {
            return sessionManager.createSession(user.getId());
        }
        return null; // Invalid credentials
    }

    public void register(String username, String password, String email) throws SQLException {
        User user = dbManager.getUserByUsername(username);
        if (user != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (dbManager.getUserByEmail(email) != null) {
            throw new IllegalArgumentException("Email already exists");
        }
        String hashedPassword = Security.hashPassword(password);
        user = new User(0, username, hashedPassword, email, DEFAULT_PROFILE_PICTURE_PATH); // Set default profile picture path
        dbManager.createUser(user);
    }
    public void logout(User user) {
        // Handle logout logic if needed
    }
}
