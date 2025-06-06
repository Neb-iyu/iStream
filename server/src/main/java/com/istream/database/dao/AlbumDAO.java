package com.istream.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.istream.model.Album;
import com.istream.model.Song;
import com.istream.database.DatabaseManager;

public class AlbumDAO implements BaseDAO {
    private final DatabaseManager dbManager;
    private final SongDAO songDAO;

    public AlbumDAO(DatabaseManager dbManager, SongDAO songDAO) {
        this.dbManager = dbManager;
        this.songDAO = songDAO;
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

    public int insertAlbum(Album album) throws SQLException {
        String sql = "INSERT INTO albums (title, artist_id, cover_art_path) VALUES (?, ?, ?) RETURNING id";
        String sql2 = "INSERT INTO album_items (album_id, song_id) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, album.getTitle());
            pstmt.setInt(2, album.getArtistId());
            pstmt.setString(3, album.getCoverArtPath());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                    pstmt2.setInt(1, rs.getInt("id"));
                    for (Song song : album.getSongs()) {
                        pstmt2.setInt(2, song.getId());
                        pstmt2.executeUpdate();
                    }
                }
                return rs.getInt("id");
            }
        }
        throw new SQLException("Failed to insert album");
    }

    public void insertAlbumItems(int albumId, int songId) throws SQLException {
        String sql = "INSERT INTO album_items (album_id, song_id) VALUES (?, ?)";
        try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, albumId);
                pstmt.setInt(2, songId);
                pstmt.executeUpdate();
        }
    }
    public Album getAlbumById(int id) throws SQLException {
        String sql = "SELECT * FROM albums WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    int artistId = rs.getInt("artist_id");
                    String coverArtPath = rs.getString("cover_art_path");
                    
                    Album album = new Album(id, title, artistId, coverArtPath, null);
                    album.setSongs(songDAO.getSongsByAlbum(id));
                    return album;
                }
            }
        }
        return null;
    }

    public List<Album> getAlbumsByTitle(String titles) throws SQLException {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums WHERE title LIKE ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + titles + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    int artistId = rs.getInt("artist_id");
                    String coverArtPath = rs.getString("cover_art_path");
                    
                    Album album = new Album(id, title, artistId, coverArtPath, null);
                    album.setSongs(songDAO.getSongsByAlbum(id));
                    albums.add(album);
                }
            }
        }
        return albums;
    }
    public List<Album> getAlbumsByArtist(int artistId) throws SQLException {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums WHERE artist_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String coverArtPath = rs.getString("cover_art_path");
                    
                    Album album = new Album(id, title, artistId, coverArtPath, null);
                    album.setSongs(songDAO.getSongsByAlbum(id));
                    albums.add(album);
                }
            }
        }
        return albums;
    }

    public List<Album> searchAlbums(String query) throws SQLException {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT a.* FROM albums a " +
                    "JOIN artists ar ON a.artist_id = ar.id " +
                    "WHERE a.title ILIKE ? OR ar.name ILIKE ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                int artistId = rs.getInt("artist_id");
                String coverArtPath = rs.getString("cover_art_path");
                
                Album album = new Album(id, title, artistId, coverArtPath, null);
                album.setSongs(songDAO.getSongsByAlbum(id));
                albums.add(album);
            }
        }
        return albums;
    }

    public List<Album> getAllAlbums() throws SQLException {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                int artistId = rs.getInt("artist_id");
                String coverArtPath = rs.getString("cover_art_path");
                
                Album album = new Album(id, title, artistId, coverArtPath, null);
                album.setSongs(songDAO.getSongsByAlbum(id));
                albums.add(album);
            }
        }
        return albums;
    }

    public int addAlbum(Album album) throws SQLException {
        String sql = "INSERT INTO albums (title, artist_id, cover_art_path) VALUES (?, ?, ?) RETURNING id";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, album.getTitle());
            pstmt.setInt(2, album.getArtistId());
            pstmt.setString(3, album.getCoverArtPath());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        throw new SQLException("Failed to create album");
    }

    public void updateAlbum(Album album) throws SQLException {
        String sql = "UPDATE albums SET title = ?, artist_id = ?, cover_art_path = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, album.getTitle());
            pstmt.setInt(2, album.getArtistId());
            pstmt.setString(3, album.getCoverArtPath());
            pstmt.setInt(4, album.getId());
            
            pstmt.executeUpdate();
        }
    }

    public void deleteAlbum(int id) throws SQLException {
        String sql = "DELETE FROM albums WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    public Album getAlbumByName(String name) throws SQLException {
        String sql = "SELECT * FROM albums WHERE title = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Album(rs.getInt("id"), rs.getString("title"), rs.getInt("artist_id"), rs.getString("cover_art_path"), null);
                }
            }
        }
        return null;
    }
}