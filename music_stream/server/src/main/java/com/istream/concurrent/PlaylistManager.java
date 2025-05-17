package com.istream.concurrent;

import com.istream.database.DatabaseManager;
import com.istream.model.Playlist;
import com.istream.model.Song;

import java.sql.SQLException;
import java.util.List;
public class PlaylistManager {
    public final DatabaseManager dbManager;
    
    public PlaylistManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<Playlist> getUserPlaylists(int userId) throws SQLException {
        return dbManager.getUserPlaylists(userId);
    }
    public Playlist createPlaylist(int userId, String name) throws SQLException {
        return dbManager.createPlaylist(userId, name);
    }

    public void addSongToPlaylist(int playlistId, int songId) throws SQLException {
        dbManager.addSongToPlaylist(playlistId, songId);
    }

    public void removeSongFromPlaylist(int playlistId, int songId) throws SQLException  {
        dbManager.removeSongFromPlaylist(playlistId, songId);
    }

    public void deletePlaylist(int playlistId) throws SQLException {
        dbManager.deletePlaylist(playlistId);
    }

    public Playlist getPlaylist(int playlistId) throws SQLException {
        return dbManager.getPlaylist(playlistId);
    }

    public List<Song> getSongsByPlaylistId(int playlistId) throws SQLException {
        return dbManager.getSongsByPlaylistId(playlistId);
    }
}
