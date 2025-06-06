package com.istream.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import com.istream.database.dao.AlbumDAO;
import com.istream.database.dao.ArtistDAO;
import com.istream.database.dao.HistoryDAO;
import com.istream.database.dao.PlaylistDAO;
import com.istream.database.dao.SongDAO;
import com.istream.database.dao.UserDAO;

public class DatabaseManager {
    private static final int SESSION_CLEANUP_INTERVAL = 30; // minutes
    private static final String URL = "jdbc:postgresql://localhost:5432/musicdb";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "1234";
    
    private final SongDAO songDAO;
    private final AlbumDAO albumDAO;
    private final ArtistDAO artistDAO;
    private final UserDAO userDAO;
    private final PlaylistDAO playlistDAO;
    private final HistoryDAO historyDAO;
    
    public DatabaseManager() {
        try {
            // Initialize DAOs
            this.songDAO = new SongDAO(this);
            this.albumDAO = new AlbumDAO(this, songDAO);
            this.artistDAO = new ArtistDAO(this, songDAO, albumDAO);
            this.userDAO = new UserDAO(this);
            this.playlistDAO = new PlaylistDAO(this, songDAO, userDAO);
            this.historyDAO = new HistoryDAO(this, songDAO, userDAO);
            
            createTablesIfNotExists();
            startSessionCleanup();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    private void startSessionCleanup() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    cleanupExpiredSessions();
                    Thread.sleep(TimeUnit.MINUTES.toMillis(SESSION_CLEANUP_INTERVAL));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }
    
    private void cleanupExpiredSessions() throws SQLException {
        String sql = "DELETE FROM sessions WHERE expires_at < NOW()";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    private void createTablesIfNotExists() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Artists table
            stmt.execute("CREATE TABLE IF NOT EXISTS artists (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "bio TEXT, " +
                "image VARCHAR(255))");

            // Albums table
            stmt.execute("CREATE TABLE IF NOT EXISTS albums (" +
                "id SERIAL PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "artist_id INT NOT NULL, " +
                "cover_art_path VARCHAR(255), " +
                "FOREIGN KEY (artist_id) REFERENCES artists(id))");

            // Songs table
            stmt.execute("CREATE TABLE IF NOT EXISTS songs (" +
                "id SERIAL PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "artist_id INT NOT NULL, " +
                "album_id INT, " +
                "genre VARCHAR(255), " +
                "duration INT NOT NULL, " +
                "file_path VARCHAR(255) NOT NULL, " +
                "cover_art_path VARCHAR(255), " +
                "year INT, " +
                "FOREIGN KEY (artist_id) REFERENCES artists(id), " +
                "FOREIGN KEY (album_id) REFERENCES albums(id))");

            // Album items table
            stmt.execute("CREATE TABLE IF NOT EXISTS album_items (" +
                "album_id INT REFERENCES albums(id), " +
                "song_id INT REFERENCES songs(id), " +
                "PRIMARY KEY (album_id, song_id))");

            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "username VARCHAR(255) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) UNIQUE NOT NULL, " +
                "profile_picture VARCHAR(255))");

            // Admins table
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                "user_id INT PRIMARY KEY, " +
                "FOREIGN KEY (user_id) REFERENCES users(id))");

            // Playlists table
            stmt.execute("CREATE TABLE IF NOT EXISTS playlists (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "name VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "description TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id))");

            // Playlist items table
            stmt.execute("CREATE TABLE IF NOT EXISTS playlist_items (" +
                "playlist_id INT REFERENCES playlists(id), " +
                "song_id INT REFERENCES songs(id), " +
                "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (playlist_id, song_id))");
            
            // Play history table
            stmt.execute("CREATE TABLE IF NOT EXISTS play_history (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "song_id INT NOT NULL, " +
                "played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (song_id) REFERENCES songs(id))");

            // Liked songs table
            stmt.execute("CREATE TABLE IF NOT EXISTS liked_songs (" +
                "user_id INT NOT NULL, " +
                "song_id INT NOT NULL, " +
                "liked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (user_id, song_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (song_id) REFERENCES songs(id))");

            // Sessions table
            stmt.execute("CREATE TABLE IF NOT EXISTS sessions (" +
                "token VARCHAR(64) PRIMARY KEY, " +
                "user_id INT NOT NULL, " + 
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "expires_at TIMESTAMP NOT NULL, " + 
                "FOREIGN KEY (user_id) REFERENCES users(id))");
        }
    }
    
    // Getters for DAOs
    public SongDAO getSongDAO() {
        return songDAO;
    }

    public AlbumDAO getAlbumDAO() {
        return albumDAO;
    }

    public ArtistDAO getArtistDAO() {
        return artistDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public PlaylistDAO getPlaylistDAO() {
        return playlistDAO;
    }

    public HistoryDAO getHistoryDAO() {
        return historyDAO;
    }
}