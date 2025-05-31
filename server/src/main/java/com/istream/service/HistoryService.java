package com.istream.service;

import java.sql.SQLException;
import java.util.List;
import com.istream.database.DatabaseManager;
import com.istream.database.dao.HistoryDAO;
import com.istream.model.*;

public class HistoryService {
    private final DatabaseManager dbManager;
    private final HistoryDAO historyDAO;

    public HistoryService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.historyDAO = dbManager.getHistoryDAO();
    }

    public List<PlayHistory> getUserHistory(int userId) throws SQLException {
        return historyDAO.getUserHistory(userId);
    }

    public List<Song> getHistorySongs(int userId) throws SQLException {
        return historyDAO.getHistorySongs(userId);
    }

    public void addToHistory(int userId, int songId) throws SQLException {
        historyDAO.addToHistory(userId, songId);
    }

    public void clearHistory(int userId) throws SQLException {
        historyDAO.clearHistory(userId);
    }
}
