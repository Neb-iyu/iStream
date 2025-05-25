package com.istream.concurrent;

import com.istream.database.DatabaseManager;
import com.istream.database.dao.PlaylistDAO;
import com.istream.model.Playlist;
import com.istream.model.Song;

import java.sql.SQLException;
import java.util.List;

public class PlaylistManager {
    private final DatabaseManager dbManager;
    private final PlaylistDAO playlistDAO;
    
    public PlaylistManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.playlistDAO = dbManager.getPlaylistDAO();
    }

    public List<Playlist> getUserPlaylists(int userId) throws SQLException {
        return playlistDAO.getUserPlaylists(userId);
    }

    public Playlist createPlaylist(int userId, String name) throws SQLException {
        return playlistDAO.createPlaylist(userId, name);
    }

    public void addSongToPlaylist(int playlistId, int songId) throws SQLException {
        playlistDAO.addSongToPlaylist(playlistId, songId);
    }

    public void removeSongFromPlaylist(int playlistId, int songId) throws SQLException {
        playlistDAO.removeSongFromPlaylist(playlistId, songId);
    }

    public void deletePlaylist(int playlistId) throws SQLException {
        playlistDAO.deletePlaylist(playlistId);
    }

    public Playlist getPlaylist(int playlistId) throws SQLException {
        return playlistDAO.getPlaylist(playlistId);
    }

    public List<Song> getSongsByPlaylistId(int playlistId) throws SQLException {
        return playlistDAO.getSongsByPlaylistId(playlistId);
    }
}
