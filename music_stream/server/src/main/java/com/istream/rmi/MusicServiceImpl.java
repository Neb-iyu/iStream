package com.istream.rmi;

import java.io.IOException;
import java.sql.SQLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.istream.database.DatabaseManager;
import com.istream.concurrent.SongLibrary;
import com.istream.concurrent.PlaylistManager;
import com.istream.file.FileManager;
import com.istream.model.*;
import com.istream.service.AuthService;
import com.istream.service.SessionManager;

public class MusicServiceImpl extends UnicastRemoteObject implements MusicService {
    private final DatabaseManager dbManager;
    private final SongLibrary songLibrary;
    private final PlaylistManager playlistManager;
    private final FileManager songfile; // For file operations
    private final SessionManager sessionManager; // For session management
    private final AuthService authService; // For authentication

    // Concurrent collections for in-memory data
    private final ConcurrentHashMap<Integer, byte[]> songCache = new ConcurrentHashMap<>();
    
    public MusicServiceImpl() throws RemoteException, SQLException {
        this.dbManager = new DatabaseManager();
        this.songLibrary = new SongLibrary(dbManager);
        this.playlistManager = new PlaylistManager(dbManager);
        this.songfile = new FileManager();
        this.sessionManager = new SessionManager();
        this.authService = new AuthService(dbManager, sessionManager);
    }

    public void validateToken(String token) throws RemoteException {
        if (!sessionManager.validateSession(token)) {
            throw new RemoteException("Invalid session token");
        }
    }
    @Override
    public List<Song> getAllSongs() throws RemoteException {
        return songLibrary.getAllSongs();
    }

    @Override
    public byte[] streamSong(int songId) throws RemoteException {
        // Check cache first
        if (songCache.containsKey(songId)) {
            return songCache.get(songId);
        }
        
        // If not in cache, check if the song is available in the file system
        byte[] songData = songfile.getSongData(songId);
        if (songData != null) {
            // Cache for future requests
            songCache.put(songId, songData);
        }
        return songData;
    }

    
    @Override
    public synchronized void uploadSong(Song song, byte[] fileData) 
            throws RemoteException {
        // Thread-safe upload process
        try {
            // Check if the song already exists
            if (songLibrary.getSongById(song.getId()) != null) {
                throw new RemoteException("Song already exists with ID: " + song.getId());
            }
            else {
                int songId = dbManager.insertSong(song);
                songfile.storeSongData(songId, fileData);   
                songCache.put(songId, fileData); // Cache the song data
                songLibrary.refresh(); // Update in-memory cache
            }
        } catch (SQLException e) {
            throw new RemoteException("Error storing song in database", e);
        } catch (IOException e) {
            throw new RemoteException("Error storing song data", e);
        }
    }
    
    @Override
    public Song getSongById(int id) throws RemoteException {
        return songLibrary.getSongById(id);
    }

    //Album operations
    @Override
    public int insertAlbum(Album album) throws RemoteException {
        try {
            return dbManager.insertAlbum(album);
        } catch (SQLException e) {
            throw new RemoteException("Error inserting album", e);
        }
    }
    @Override
    public Album getAlbumById(int id) throws RemoteException {
        try {
            return dbManager.getAlbumById(id);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching album", e);
        }
    }
    @Override
    public List<Album> getAlbumsByArtist(String artist) throws RemoteException {
        try {
            return dbManager.getAlbumsByArtist(artist);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching albums by artist", e);
        }
    }
    @Override
    public List<Album> getAlbumsByTitle(String title) throws RemoteException {
        try {
            return dbManager.getAlbumsByTitle(title);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching albums by title", e);
        }
    }
    @Override
    public List<Playlist> getUserPlaylists(int userId) throws RemoteException {
        try {
            return playlistManager.getUserPlaylists(userId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching user playlists", e);
        }
    }

    @Override
    public Playlist createPlaylist(int userId, String name) throws RemoteException {
        try {
            return playlistManager.createPlaylist(userId, name);
        } catch (SQLException e) {
            throw new RemoteException("Error creating playlist", e);
        }
    }

    @Override
    public void addSongToPlaylist(int playlistId, int songId) throws RemoteException {
        try {
            playlistManager.addSongToPlaylist(playlistId, songId);
        }
        catch (SQLException e) {
            throw new RemoteException("Error adding song to playlist", e);
        }
        //playlistManager.addSongToPlaylist(playlistId, songId);
    }

    @Override
    public Playlist getPlaylist(int playlistId) throws RemoteException {
        try {
            return playlistManager.getPlaylist(playlistId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching playlist", e);
        }
    }

    @Override
    public List<Song> getSongsByPlaylistId(int playlistId) throws RemoteException {
        try {
            return playlistManager.getSongsByPlaylistId(playlistId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching songs by playlist ID", e);
        }
    }

    @Override
    public void recordPlay(int userId, int songId) throws RemoteException {
        try {
            dbManager.recordPlay(userId, songId);
        } catch (SQLException e) {
            throw new RemoteException("Error recording play", e);
        }
    }

    @Override
    public void likeSong(int userId, int songId) throws RemoteException {
        try {
            dbManager.likeSong(userId, songId);
        } catch (SQLException e) {
            throw new RemoteException("Error liking song", e);
        }
    }

    @Override
    public List<PlayHistory> getHistory(int userId) throws RemoteException {
        try {
            return dbManager.getUserHistory(userId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching user history", e);
        }
    }
    @Override
    public Boolean register(String username, String password, String email) throws RemoteException {
        try {
            authService.register(username, password, email);
            return true;
        } catch (SQLException e) {
            throw new RemoteException("Error registering user", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteException("Invalid registration data", e);
        }
        
    }
    //Artist operations
    @Override
    public List<Artist> getAllArtists() throws RemoteException {
        try {
            return dbManager.getAllArtists();
        } catch (SQLException e) {
            throw new RemoteException("Error fetching all artists", e);
        }
    }
    @Override
    public Artist getArtistById(int id) throws RemoteException {
        try {   
            return dbManager.getArtistById(id);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching artist by ID", e);
        }
    }
    @Override
    public List<Artist> getArtistsByName(String name) throws RemoteException {
        try {
            return dbManager.getArtistsByName(name);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching artists by name", e);
        }
    }
    

    @Override
    public String login(String username, String password) throws RemoteException {
        try {
            return authService.login(username, password);
        } catch (SQLException e) {
            throw new RemoteException("Error during login", e);
        }
    }

    @Override
    public void logout(String authToken) throws RemoteException {
        sessionManager.invalidateSession(authToken);
    }

    @Override
    public boolean isLoggedIn(String authToken) throws RemoteException {
        return sessionManager.validateSession(authToken);
    }
}
    