package com.istream.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import com.istream.concurrent.PlaylistManager;
import com.istream.concurrent.SongLibrary;
import com.istream.database.DatabaseManager;
import com.istream.database.dao.*;
import com.istream.file.FileManager;
import com.istream.model.*;
import com.istream.service.AuthService;
import com.istream.service.SessionManager;

public class MusicServiceImpl extends UnicastRemoteObject implements MusicService {
    private final DatabaseManager dbManager;
    private final SongLibrary songLibrary;
    private final PlaylistManager playlistManager;
    private final FileManager fileManager;
    private final SessionManager sessionManager;
    private final AuthService authService;
    private final Map<String, Long> activeSessions;
    private final Set<Integer> activeStreams;
    private static final long SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(30);
    private static final long STREAM_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    private final ConcurrentHashMap<Integer, byte[]> songCache = new ConcurrentHashMap<>();
    private final SongDAO songDAO;
    private final AlbumDAO albumDAO;
    private final ArtistDAO artistDAO;
    private final UserDAO userDAO;
    private final HistoryDAO historyDAO;
    
    public MusicServiceImpl() throws RemoteException, SQLException {
        super();
        this.dbManager = new DatabaseManager();
        this.songDAO = dbManager.getSongDAO();
        this.albumDAO = dbManager.getAlbumDAO();
        this.artistDAO = dbManager.getArtistDAO();
        this.userDAO = dbManager.getUserDAO();
        this.historyDAO = dbManager.getHistoryDAO();
        this.songLibrary = new SongLibrary(dbManager, "songs");
        this.playlistManager = new PlaylistManager(dbManager);
        this.fileManager = new FileManager();
        this.sessionManager = new SessionManager();
        this.authService = new AuthService(dbManager, sessionManager);
        this.activeSessions = new ConcurrentHashMap<>();
        this.activeStreams = ConcurrentHashMap.newKeySet();
    }

    private void validateSession(String token) throws RemoteException {
        if (!sessionManager.validateSession(token)) {
            throw new RemoteException("Invalid session");
        }
    }

    private int getUserId(String token) throws RemoteException {
        Integer userId = sessionManager.getUserIdFromToken(token);
        if (userId == null) {
            throw new RemoteException("Invalid session");
        }
        return userId;
    }

    @Override
    public List<Song> getAllSongs(String token) throws RemoteException {
        validateSession(token);
        return songLibrary.getAllSongs();
    }

    @Override
    public List<Song> getSongsByAlbumId(int albumId, String token) throws RemoteException {
        validateSession(token);
        return songLibrary.getSongsByAlbumId(albumId);
    }

    @Override
    public List<Song> getSongsByArtistId(int artistId, String token) throws RemoteException {
        validateSession(token);
        return songLibrary.getSongsByArtistId(artistId);
    }

    @Override
    public List<Song> getSongsByName(String name) throws RemoteException {
        return songLibrary.getSongsByName(name);
    }

    @Override
    public List<Song> getLikedSongs(String token) throws RemoteException {
        int userId = getUserId(token);
        return songLibrary.getLikedSongs(userId);
    }

    @Override
    public byte[] streamSong(int songId) throws RemoteException {
        activeStreams.add(songId);
        try {
            return songLibrary.getSongData(songId);
        } finally {
            activeStreams.remove(songId);
        }
    }

    @Override
    public synchronized void uploadSong(Song song, byte[] fileData, String artistName, String albumName) 
            throws RemoteException {
        try {
            if (songLibrary.getSongById(song.getId()) != null) {
                throw new RemoteException("Song already exists with ID: " + song.getId());
            }
            
            Artist artist = artistDAO.getArtistByName(artistName);
            Album album = albumDAO.getAlbumByName(albumName);
            
            if (artist == null) {
                artist = new Artist(0, artistName, "", new ArrayList<>(), new ArrayList<>());
                int artistId = artistDAO.addArtist(artist);
                artist.setId(artistId);
            }
            
            if (album == null) {
                album = new Album(0, albumName, artist.getId(), "", null);
                int albumId = albumDAO.addAlbum(album);
                album.setId(albumId);
            }
            
            song.setArtistId(artist.getId());
            song.setAlbumId(album.getId());
            int songId = songDAO.insertSong(song);
            
            fileManager.storeSongData(songId, fileData);
            songLibrary.refresh();
        } catch (SQLException e) {
            throw new RemoteException("Error storing song in database", e);
        } catch (IOException e) {
            throw new RemoteException("Error storing song data", e);
        }
    }

    @Override
    public Song getSongById(int songId, String token) throws RemoteException {
        validateSession(token);
        return songLibrary.getSongById(songId);
    }

    //Album operations
    @Override
    public int insertAlbum(Album album) throws RemoteException {
        try {
            return albumDAO.insertAlbum(album);
        } catch (SQLException e) {
            throw new RemoteException("Error inserting album", e);
        }
    }
    @Override
    public Album getAlbumById(int albumId, String token) throws RemoteException {
        validateSession(token);
        try {
            return albumDAO.getAlbumById(albumId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching album", e);
        }
    }
    @Override
    public List<Album> getAlbumsByArtist(int artistId, String token) throws RemoteException {
        validateSession(token);
        try {
            return albumDAO.getAlbumsByArtist(artistId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching albums by artist", e);
        }
    }
    @Override
    public List<Album> getAlbumsByTitle(String title) throws RemoteException {
        try {
            return albumDAO.getAlbumsByTitle(title);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching albums by title", e);
        }
    }
    @Override
    public List<Album> getAllAlbums(String token) throws RemoteException {
        validateSession(token);
        try {
            return albumDAO.getAllAlbums();
        } catch (SQLException e) {
            throw new RemoteException("Error fetching all albums", e);
        }
    }

    //Playlist operations
    @Override
    public List<Playlist> getUserPlaylists(String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            return playlistManager.getUserPlaylists(userId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching user playlists", e);
        }
    }

    @Override
    public Playlist createPlaylist(String token, String name) throws RemoteException {
        int userId = getUserId(token);
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
    public void removeSongFromPlaylist(int playlistId, int songId) throws RemoteException {
        try {
            playlistManager.removeSongFromPlaylist(playlistId, songId);
        } catch (SQLException e) {
            throw new RemoteException("Error removing song from playlist", e);
        }
    }
    @Override
    public void deletePlaylist(int playlistId) throws RemoteException {
        try {
            playlistManager.deletePlaylist(playlistId);
        } catch (SQLException e) {
            throw new RemoteException("Error deleting playlist", e);
        }
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
    public void recordPlay(int songId, String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            historyDAO.addToHistory(userId, songId);
        } catch (SQLException e) {
            throw new RemoteException("Error recording play", e);
        }
    }

    @Override
    public void likeSong(int songId, String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            songDAO.likeSong(userId, songId);
        } catch (SQLException e) {
            throw new RemoteException("Error liking song", e);
        }
    }
    @Override
    public void unlikeSong(int songId, String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            songDAO.unlikeSong(userId, songId);
        } catch (SQLException e) {
            throw new RemoteException("Error unliking song", e);
        }
    }
    @Override
    public boolean isSongLiked(int songId, String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            return songDAO.isSongLiked(userId, songId);
        } catch (SQLException e) {
            throw new RemoteException("Error checking if song is liked", e);
        }
    }
    @Override
    public List<PlayHistory> getHistory(String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            return historyDAO.getUserHistory(userId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching user history", e);
        }
    }
    @Override
    public String login(String username, String password) throws RemoteException {
        try {
            System.out.println("Login attempt for user: " + username);
            String sessionToken = userDAO.authenticateUser(username, password);
            if (sessionToken != null) {
                System.out.println("Login successful for user: " + username);
                activeSessions.put(sessionToken, System.currentTimeMillis());
            } else {
                System.out.println("Login failed for user: " + username + " - Invalid credentials");
            }
            return sessionToken;
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Database error during login: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Unexpected error during login: " + e.getMessage(), e);
        }
    }

    @Override
    public void logout(String authToken) throws RemoteException {
        activeSessions.remove(authToken);
    }

    @Override
    public boolean isLoggedIn(String authToken) throws RemoteException {
        if (activeSessions.containsKey(authToken)) {
            activeSessions.put(authToken, System.currentTimeMillis()); // Update last access time
            return true;
        }
        return false;
    }

    @Override
    public byte[] getImage(String path) throws RemoteException {
        // TODO: Implement
        try {
            return fileManager.getImage(path);
        } catch (IOException e) {
            throw new RemoteException("Error fetching image", e);
        }
    }

    @Override
    public void saveImage(String path, byte[] image) throws RemoteException {
        // TODO: Implement
        try {
            fileManager.saveImage(path, image);
        } catch (IOException e) {
            throw new RemoteException("Error saving image", e);
        }
    }

    public void cleanupOldSessions() {
        long currentTime = System.currentTimeMillis();
        
        // Cleanup expired sessions
        activeSessions.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > SESSION_TIMEOUT
        );

        // Cleanup expired streams
        activeStreams.clear(); // Reset active streams periodically

        // Force garbage collection of unused resources
        System.gc();
    }

    @Override
    public List<Album> searchAlbums(String query, String token) throws RemoteException {
        validateSession(token);
        try {
            return albumDAO.searchAlbums(query);
        } catch (SQLException e) {
            throw new RemoteException("Error searching albums", e);
        }
    }

    @Override
    public List<Artist> searchArtists(String query, String token) throws RemoteException {
        validateSession(token);
        try {
            return artistDAO.getArtistsByName(query);
        } catch (SQLException e) {
            throw new RemoteException("Error searching artists", e);
        }
    }

    @Override
    public List<Song> searchSongs(String query, String token) throws RemoteException {
        validateSession(token);
        return songLibrary.searchSongs(query);
    }

    @Override
    public void clearHistory(String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            historyDAO.clearHistory(userId);
        } catch (SQLException e) {
            throw new RemoteException("Error clearing history", e);
        }
    }

    @Override
    public List<Song> getHistorySongs(String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            return historyDAO.getHistorySongs(userId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching history songs", e);
        }
    }

    @Override
    public Boolean register(String username, String password, String email) throws RemoteException {
        try {
            System.out.println("Registration attempt for user: " + username);
            authService.register(username, password, email);
            System.out.println("Registration successful for user: " + username);
            return true;
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Database error during registration: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid registration data: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Invalid registration data: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Unexpected error during registration: " + e.getMessage(), e);
        }
    }

    @Override
    public User getUserById(String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            return userDAO.getUserById(userId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching user by ID", e);
        }
    }

    @Override
    public void addUser(User user) throws RemoteException {
        try {
            userDAO.createUser(user);
        } catch (SQLException e) {
            throw new RemoteException("Error adding user", e);
        }
    }
    

    //Admin operations
    @Override
    public void addAdmin(int userId) throws RemoteException {
        try {
            userDAO.addAdmin(userId);
        } catch (SQLException e) {
            throw new RemoteException("Error adding admin", e);
        }
    }
    @Override
    public void removeAdmin(int userId) throws RemoteException {
        try {
            userDAO.removeAdmin(userId);
        } catch (SQLException e) {
            throw new RemoteException("Error removing admin", e);
        }
    }
    @Override
    public boolean isAdmin(int userId) throws RemoteException {
        try {
            return userDAO.isAdmin(userId);
        } catch (SQLException e) {
            throw new RemoteException("Error checking admin status", e);
        }
    }
    
    //Artist operations
    @Override
    public List<Artist> getAllArtists(String token) throws RemoteException {
        validateSession(token);
        try {
            return artistDAO.getAllArtists();
        } catch (SQLException e) {
            throw new RemoteException("Error fetching all artists", e);
        }
    }

    @Override
    public Artist getArtistById(int artistId, String token) throws RemoteException {
        validateSession(token);
        try {   
            return artistDAO.getArtistById(artistId);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching artist by ID", e);
        }
    }
    @Override
    public List<Artist> getArtistsByName(String name) throws RemoteException {
        try {
            return artistDAO.getArtistsByName(name);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching artists by name", e);
        }
    }
    @Override
    public void addArtist(Artist artist) throws RemoteException {
        try {
            artistDAO.addArtist(artist);
        } catch (SQLException e) {
            throw new RemoteException("Error adding artist", e);
        }
    }

}
    