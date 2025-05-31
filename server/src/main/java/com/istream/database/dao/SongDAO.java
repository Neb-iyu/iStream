package com.istream.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.istream.database.DatabaseManager;
import com.istream.model.Song;

public class SongDAO implements BaseDAO {
    private final DatabaseManager dbManager;

    public SongDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
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

    public int insertSong(Song song) throws SQLException {
        String sql = "INSERT INTO songs (title, artist_id, album_id, duration, file_path, cover_art_path, year) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, song.getTitle());
            pstmt.setInt(2, song.getArtistId());
            pstmt.setInt(3, song.getAlbumId());
            pstmt.setInt(4, song.getDuration());
            pstmt.setString(5, song.getFilePath());
            pstmt.setString(6, song.getCoverArtPath());
            pstmt.setInt(7, song.getYear());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to insert song");
    }

    public List<Song> getAllSongs() throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Song song = new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("artist_id"),
                    rs.getInt("album_id"),
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
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("artist_id"),
                    rs.getInt("album_id"),
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

    public List<Song> getSongsByArtist(int artistId) throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE artist_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Song song = new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("artist_id"),
                    rs.getInt("album_id"),
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

    public List<Song> getSongsByAlbum(int albumId) throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.* FROM songs s " +
                    "JOIN album_items ai ON s.id = ai.song_id " +
                    "WHERE ai.album_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, albumId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Song song = new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("artist_id"),
                    rs.getInt("album_id"),
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

    public List<Song> getSongsByTitle(String title) throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE title ILIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + title + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                songs.add(getSongById(rs.getInt("id")));
            }
        }
        return songs;
    }

    public List<Song> getLikedSongs(int userId) throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.* FROM songs s " +
                    "JOIN liked_songs ls ON s.id = ls.song_id " +
                    "WHERE ls.user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Song song = new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("artist_id"),
                    rs.getInt("album_id"),
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

    public void likeSong(int userId, int songId) throws SQLException {
        String sql = "INSERT INTO liked_songs (user_id, song_id) VALUES (?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            pstmt.executeUpdate();
        }
    }

    public void unlikeSong(int userId, int songId) throws SQLException {
        String sql = "DELETE FROM liked_songs WHERE user_id = ? AND song_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            pstmt.executeUpdate();
        }
    }

    public boolean isSongLiked(int userId, int songId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM liked_songs WHERE user_id = ? AND song_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, songId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<Song> searchSongs(String query) throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.* FROM songs s " +
                    "JOIN artists a ON s.artist_id = a.id " +
                    "WHERE s.title ILIKE ? OR a.name ILIKE ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Song song = new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("artist_id"),
                    rs.getInt("album_id"),
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

    public void updateSong(Song song) throws SQLException {
        String sql = "UPDATE songs SET title = ?, artist_id = ?, album_id = ?, genre = ?, " +
                    "duration = ?, file_path = ?, cover_art_path = ?, year = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, song.getTitle());
            pstmt.setInt(2, song.getArtistId());
            pstmt.setInt(3, song.getAlbumId());
            pstmt.setString(4, song.getGenre());
            pstmt.setInt(5, song.getDuration());
            pstmt.setString(6, song.getFilePath());
            pstmt.setString(7, song.getCoverArtPath());
            pstmt.setInt(8, song.getYear());
            pstmt.setInt(9, song.getId());
            
            pstmt.executeUpdate();
        }
    }
} 