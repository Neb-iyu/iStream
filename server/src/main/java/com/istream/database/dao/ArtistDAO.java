package com.istream.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.istream.database.DatabaseManager;
import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.Song;

public class ArtistDAO implements BaseDAO {
    private final DatabaseManager dbManager;
    private final SongDAO songDAO;
    private final AlbumDAO albumDAO;

    public ArtistDAO(DatabaseManager dbManager, SongDAO songDAO, AlbumDAO albumDAO) {
        this.dbManager = dbManager;
        this.songDAO = songDAO;
        this.albumDAO = albumDAO;
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

    public List<Artist> getAllArtists() throws SQLException {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT * FROM artists";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Artist artist = new Artist(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("image"),
                    null,
                    null
                );
                artists.add(artist);
            }
        }
        return artists;
    }

    public Artist getArtistById(int id) throws SQLException {
        String sql = "SELECT * FROM artists WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                List<Song> songs = songDAO.getSongsByArtist(id);
                List<Album> albums = albumDAO.getAlbumsByArtist(id);
                Artist artist = new Artist(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("image"),
                    songs,
                    albums
                );
                artist.setSongs(songs);
                artist.setAlbums(albums);
                return artist;
            }
        }
        return null;
    }

    public List<Artist> getArtistsByName(String name) throws SQLException {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT * FROM artists WHERE name ILIKE ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Artist artist = new Artist(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("image"),
                    null,
                    null
                );
                artists.add(artist);
            }
        }
        return artists;
    }

    public int addArtist(Artist artist) throws SQLException {
        String sql = "INSERT INTO artists (name, image) VALUES (?, ?) RETURNING id";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, artist.getName());
            pstmt.setString(2, artist.getImageUrl());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        throw new SQLException("Failed to insert artist");
    }
    public Artist getArtistByName(String name) throws SQLException {
        String sql = "SELECT * FROM artists WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                List<Song> songs = songDAO.getSongsByArtist(rs.getInt("id"));
                List<Album> albums = albumDAO.getAlbumsByArtist(rs.getInt("id"));
                Artist artist = new Artist(rs.getInt("id"), rs.getString("name"), rs.getString("image"), songs, albums);
                return artist;
            }
        }
        return null;
    }
} 