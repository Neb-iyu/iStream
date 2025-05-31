package com.istream.service;

import java.sql.SQLException;
import com.istream.database.DatabaseManager;
import com.istream.database.dao.UserDAO;
import com.istream.model.User;
import com.istream.util.Security;

import com.istream.model.Session;

public class AuthService {
    private static final String DEFAULT_PROFILE_PICTURE_PATH = "/resources/images/default-profile.png"; // Default profile picture path

    private final DatabaseManager dbManager;
    private final UserDAO userDAO;
    private final SessionManager sessionManager;

    public AuthService() {
        this.dbManager = new DatabaseManager();
        this.userDAO = dbManager.getUserDAO();
        this.sessionManager = new SessionManager();
    }

    public AuthService(DatabaseManager dbManager, SessionManager sessionManager) {
        this.dbManager = dbManager;
        this.userDAO = dbManager.getUserDAO();
        this.sessionManager = sessionManager;
    }

    public String login(String username, String password) throws SQLException {
        User user = userDAO.getUserByUsername(username);
        if (user != null && Security.verifyPassword(password, user.getPassword())) {
            return sessionManager.createSession(user.getId());
        }
        return null;
    }

    public String register(String username, String password, String email) throws SQLException {
        if (userDAO.getUserByUsername(username) != null) {
            return null; // Username already exists
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(Security.hashPassword(password));
        newUser.setEmail(email);
        newUser.setProfilePicture(DEFAULT_PROFILE_PICTURE_PATH);

        int userId = userDAO.createUser(newUser);
        return sessionManager.createSession(userId);
    }

    public boolean validateSession(String token) {
        return sessionManager.validateSession(token);
    }

    public void logout(String token) {
        sessionManager.invalidateSession(token);
    }

    public Integer getUserIdFromToken(String token) {
        return sessionManager.getUserIdFromToken(token);
    }
}
