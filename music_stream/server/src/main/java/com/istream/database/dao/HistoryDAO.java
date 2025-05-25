package com.istream.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.istream.database.DatabaseManager;
import com.istream.model.PlayHistory;
import com.istream.model.Song;
import com.istream.model.User;

public class HistoryDAO implements BaseDAO {
    private final DatabaseManager dbManager;
    private final SongDAO songDAO;
    private final UserDAO userDAO;

    public HistoryDAO(DatabaseManager dbManager, SongDAO songDAO, UserDAO userDAO) {
        this.dbManager = dbManager;
        this.songDAO = songDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dbManager.getConnection();
    }

    @Override
    public void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addToHistory(int userId, int songId) throws SQLException {
        String sql = "INSERT INTO play_history (user_id, song_id, played_at) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        }
    }

    public List<PlayHistory> getUserHistory(int userId) throws SQLException {
        List<PlayHistory> history = new ArrayList<>();
        String sql = "SELECT * FROM play_history WHERE user_id = ? ORDER BY played_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Song song = songDAO.getSongById(rs.getInt("song_id"));
                if (song != null) {
                    history.add(new PlayHistory(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("song_id"),
                        rs.getTimestamp("played_at").toLocalDateTime()
                    ));
                }
            }
        }
        return history;
    }

    public void clearHistory(int userId) throws SQLException {
        String sql = "DELETE FROM play_history WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    public List<PlayHistory> getRecentHistory(int userId, int limit) throws SQLException {
        List<PlayHistory> history = new ArrayList<>();
        String sql = "SELECT * FROM play_history WHERE user_id = ? ORDER BY played_at DESC LIMIT ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Song song = songDAO.getSongById(rs.getInt("song_id"));
                if (song != null) {
                    history.add(new PlayHistory(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("song_id"),
                        rs.getTimestamp("played_at").toLocalDateTime()
                    ));
                }
            }
        }
        return history;
    }
    public List<Song> getHistorySongs(int userId) throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM play_history WHERE user_id = ? ORDER BY played_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Song song = songDAO.getSongById(rs.getInt("song_id"));
                if (song != null) {
                    songs.add(song);
                }
            }
        }
        return songs;
    }
} 