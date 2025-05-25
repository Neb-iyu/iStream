package com.istream.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.istream.database.DatabaseManager;
import com.istream.model.Playlist;
import com.istream.model.Song;
import com.istream.model.User;

public class PlaylistDAO implements BaseDAO {
    private final DatabaseManager dbManager;
    private final SongDAO songDAO;
    private final UserDAO userDAO;

    public PlaylistDAO(DatabaseManager dbManager, SongDAO songDAO, UserDAO userDAO) {
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

    public List<Playlist> getUserPlaylists(int userId) throws SQLException {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT * FROM playlists WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                HashSet<Song> songs = new HashSet<>();
                String sql2 = "SELECT song_id FROM playlist_items WHERE playlist_id = ?";
                try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                    pstmt2.setInt(1, rs.getInt("id"));
                    ResultSet rs2 = pstmt2.executeQuery();
                    
                    while (rs2.next()) {
                        Song song = songDAO.getSongById(rs2.getInt("song_id"));
                        if (song != null) {
                            songs.add(song);
                        }
                    }
                }
                Playlist playlist = new Playlist(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("user_id")
                );
                playlist.setSongs(songs);
                playlists.add(playlist);
            }
        }
        return playlists;
    }

    public Playlist createPlaylist(int userId, String name) throws SQLException {
        String sql = "INSERT INTO playlists (user_id, name) VALUES (?, ?) RETURNING id";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Playlist(
                    rs.getInt(1),
                    name,
                    null,
                    userId
                );
            }
        }
        throw new SQLException("Failed to create playlist");
    }

    public void addSongToPlaylist(int playlistId, int songId) throws SQLException {
        String sql = "INSERT INTO playlist_items (playlist_id, song_id) VALUES (?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, songId);
            pstmt.executeUpdate();
        }
    }

    public void removeSongFromPlaylist(int playlistId, int songId) throws SQLException {
        String sql = "DELETE FROM playlist_items WHERE playlist_id = ? AND song_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, songId);
            pstmt.executeUpdate();
        }
    }

    public void deletePlaylist(int playlistId) throws SQLException {
        String sql = "DELETE FROM playlists WHERE id = ?";
        String sql2 = "DELETE FROM playlist_items where playlist_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            pstmt.executeUpdate();
        }
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql2)) {
            pstmt.setInt(1, playlistId);
            pstmt.executeUpdate();
        }
    }

    public Playlist getPlaylist(int playlistId) throws SQLException {
        String sql = "SELECT * FROM playlists WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                HashSet<Song> songs = new HashSet<>();
                String sql2 = "SELECT song_id FROM playlist_items WHERE playlist_id = ?";
                try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                    pstmt2.setInt(1, playlistId);
                    ResultSet rs2 = pstmt2.executeQuery();
                    while (rs2.next()) {
                        songs.add(songDAO.getSongById(rs2.getInt("song_id")));
                    }
                }
                Playlist playlist = new Playlist(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("user_id")
                );
                playlist.setSongs(songs);
                return playlist;
            }
        }
        return null;
    }

    public List<Song> getSongsByPlaylistId(int playlistId) throws SQLException {
        String sql = "SELECT song_id FROM playlist_items WHERE playlist_id = ?";
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            ResultSet rs = pstmt.executeQuery();
            List<Song> songs = new ArrayList<>();
            while (rs.next()) {
                songs.add(songDAO.getSongById(rs.getInt("song_id")));
            }
            return songs;
        }
    }
} 