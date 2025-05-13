package com.istream.service;

import java.sql.SQLException;
import java.util.List;
import com.istream.database.DatabaseManager;
import com.istream.model.*;

public class HistoryService {
    private final DatabaseManager dbManager;

    public HistoryService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<PlayHistory> getUserHistory(int userId) throws SQLException {
        return dbManager.getUserHistory(userId);
    }

    public PlayHistory getPlayHistory(int userId, int songId) throws SQLException {
        return dbManager.getPlayHistory(userId, songId);
    }

    public void recordPlay(int userId, int songId, String timestamp) throws SQLException {
        dbManager.recordPlay(userId, songId, timestamp);
    }
    public void clearHistory(int userId) throws SQLException {
        dbManager.clearHistory(userId);
    }
}
