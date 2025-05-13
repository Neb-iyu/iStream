package com.istream.database;

import java.sql.*;
import java.util.ArrayList;
// import java.util.Dictionary;
// import java.util.Enumeration;
// import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import com.istream.model.*;

public class DatabaseManager {
    private Connection connection;
    
    public DatabaseManager() {
        try {
            // Initialize connection
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost/musicdb",
                "username",
                "password"
            );
            createTablesIfNotExists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createTablesIfNotExists() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Songs table
            stmt.execute("CREATE TABLE IF NOT EXISTS songs (" +
                "id SERIAL PRIMARY KEY, " +
                "title VARCHAR(255), " +
                "artist VARCHAR(255), " +
                "album VARCHAR(255), " +
                "duration INT, " +
                "file_path VARCHAR(255), " +
                "cover_art_path VARCHAR(255))");
            
            // Song data (separate table for BLOB)
            // stmt.execute("CREATE TABLE IF NOT EXISTS song_data (" +
            //     "song_id INT PRIMARY KEY REFERENCES songs(id), " +
            //     "data BYTEA)");
            
            // Users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "username VARCHAR(255) UNIQUE, " +
                "hashed_password VARCHAR(255), " +
                "email VARCHAR(255) UNIQUE)");

            // Playlists
            stmt.execute("CREATE TABLE IF NOT EXISTS playlists (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INT, " +
                "name VARCHAR(255))");
            
            // Playlist items
            stmt.execute("CREATE TABLE IF NOT EXISTS playlist_items (" +
                "playlist_id INT REFERENCES playlists(id), " +
                "song_id INT REFERENCES songs(id), " +
                "PRIMARY KEY (playlist_id, song_id))");
            
            // History
            stmt.execute("CREATE TABLE IF NOT EXISTS play_history (" +
                "user_id INT, " +
                "song_id INT REFERENCES songs(id), " +
                "play_time TIMESTAMP, " +
                "PRIMARY KEY (user_id, song_id))");
            
            // Likes
            stmt.execute("CREATE TABLE IF NOT EXISTS likes (" +
                "user_id INT, " +
                "song_id INT REFERENCES songs(id), " +
                "PRIMARY KEY (user_id, song_id))");
            //Session
            stmt.execute("CREATE TABLE IF NOT EXISTS sessions (" +
                "token VARCHAR(64) PRIMARY KEY," + 
                "user_id INT NOT NULL, " + 
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," + 
                "expires_at TIMESTAMP NOT NULL, " + 
                "FOREIGN KEY (user_id) REFERENCES users(id))");
        }
    }
    
    // Song operations
    public int insertSong(Song song) throws SQLException {
        String sql = "INSERT INTO songs (title, artist, album, duration, file_path, cover_art_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, song.getTitle());
            pstmt.setString(2, song.getArtist());
            pstmt.setString(3, song.getAlbum());
            pstmt.setInt(4, song.getDuration());
            pstmt.setString(5, song.getFilePath());
            pstmt.setString(6, song.getCoverArtPath());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to insert song");
    }
    
    // public void storeSongData(int songId, byte[] data) throws SQLException {
    //     String sql = "INSERT INTO song_data (song_id, data) VALUES (?, ?)";
        
    //     try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    //         pstmt.setInt(1, songId);
    //         pstmt.setBytes(2, data);
    //         pstmt.executeUpdate();
    //     }
    // }
    
    // public byte[] getSongData(int songId) throws SQLException {
    //     String sql = "SELECT data FROM song_data WHERE song_id = ?";
        
    //     try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    //         pstmt.setInt(1, songId);
    //         ResultSet rs = pstmt.executeQuery();
            
    //         if (rs.next()) {
    //             return rs.getBytes(1);
    //         }
    //     }
    //     return null;
    // }
    public List<Song> getAllSongs() throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Song song = new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("artist"),
                    rs.getString("album"),
                    rs.getString("genre"),
                    rs.getInt("duration"),
                    rs.getString("file_path"),
                    rs.getString("cover_art_path"),
                    rs.getInt("year")
                );
                songs.add(song);
            }
        }
        return songs;
    }
    public Song getSongById(int id) throws SQLException {
        String sql = "SELECT * FROM songs WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("artist"),
                    rs.getString("album"),
                    rs.getString("genre"),
                    rs.getInt("duration"),
                    rs.getString("file_path"),
                    rs.getString("cover_art_path"),
                    rs.getInt("year")
                );
            }
        }
        return null;
    }

    // Playlist operations
    public List<Playlist> getUserPlaylists(int userId) throws SQLException {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT * FROM playlists WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                HashSet<Song> songs = new HashSet<>();
                String sql2 = "SELECT song_id FROM playlist_items WHERE playlist_id = ?";
                try (PreparedStatement pstmt2 = connection.prepareStatement(sql2)) {
                    pstmt2.setInt(1, rs.getInt("id"));
                    ResultSet rs2 = pstmt2.executeQuery();
                    
                    while (rs2.next()) {
                        Song song = getSongById(rs2.getInt("song_id"));
                        if (song != null) {
                            songs.add(song);
                        }
                    }
                }
                User owner = getUserById(rs.getInt("user_id"));
                 Playlist playlist = new Playlist(
                     rs.getInt("id"),
                     rs.getString("name"),
                     null, // description not used here
                     owner,
                     songs,
                     0
                 );
                 playlists.add(playlist);
            }
        }
        return playlists;
    }
    public Playlist createPlaylist(int userId, String name) throws SQLException {
        String sql = "INSERT INTO playlists (user_id, name) VALUES (?, ?) RETURNING id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Playlist(rs.getInt(1), name, null, getUserById(userId), new HashSet<>(), 0);
            }
        }
        throw new SQLException("Failed to create playlist");
    }
    public void addSongToPlaylist(int playlistId, int songId) throws SQLException {
        String sql = "INSERT INTO playlist_items (playlist_id, song_id) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, songId);
            pstmt.executeUpdate();
        }
    }
    public void removeSongFromPlaylist(int playlistId, int songId) throws SQLException {
        String sql = "DELETE FROM playlist_items WHERE playlist_id = ? AND song_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, songId);
            pstmt.executeUpdate();
        }
    }
    public void deletePlaylist(int playlistId) throws SQLException {
        String sql = "DELETE FROM playlists WHERE id = ?";
        String sql2 = "DELETE FROM playlist_items where playlist_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            pstmt.executeUpdate();
        }
        try (PreparedStatement pstmt = connection.prepareStatement(sql2)) {
            pstmt.setInt(1, playlistId);
            pstmt.executeUpdate();
        }
    }
    // User operations
    public int createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, hashed_password, email) VALUES (?, ?, ?) RETURNING id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to create user");
    }
    
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("hashed_password"),
                    rs.getString("email"),
                    null
                );
            }
        }
        return null;
    }
    
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("hashed_password"),
                    rs.getString("email"),
                    null
                );
            }
        }
        return null;
    }
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("hashed_password"),
                    rs.getString("email"),
                    null
                );
            }
        }
        return null;
    }
    public void recordPlay(int userId, int songId) throws SQLException {
        String sql = "INSERT INTO play_history (user_id, song_id, play_time) VALUES (?, ?, NOW())";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            pstmt.executeUpdate();
        }
    }
    public void likeSong(int userId, int songId) throws SQLException {
        String sql = "INSERT INTO likes (user_id, song_id) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            pstmt.executeUpdate();
        }
    }
    public List<Song> getHistory(int userId) throws SQLException {
        List<Song> history = new ArrayList<>();
        String sql = "SELECT song_id FROM play_history WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Song song = getSongById(rs.getInt("song_id"));
                if (song != null) {
                    history.add(song);
                }
            }
        }
        return history;
    }
    // History operations
    public List<PlayHistory> getUserHistory(int userId) throws SQLException {
        List<PlayHistory> history = new ArrayList<>();
        String sql = "SELECT * FROM play_history WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                PlayHistory playHistory = new PlayHistory(
                    rs.getInt("user_id"),
                    rs.getInt("song_id"),
                    rs.getString("play_time")
                );
                history.add(playHistory);
            }
        }
        return history;
    }
    public PlayHistory getPlayHistory(int userId, int songId) throws SQLException {
        String sql = "SELECT * FROM play_history WHERE user_id = ? AND song_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new PlayHistory(
                    rs.getInt("user_id"),
                    rs.getInt("song_id"),
                    rs.getString("play_time")
                );
            }
        }
        return null;
    }
    public void clearHistory(int userId) throws SQLException {
        String sql = "DELETE FROM play_history WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }
    public void recordPlay(int userId, int songId, String timestamp) throws SQLException {
        String sql = "INSERT INTO play_history (user_id, song_id, play_time) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            pstmt.setString(3, timestamp);
            pstmt.executeUpdate();
        }
    }
}