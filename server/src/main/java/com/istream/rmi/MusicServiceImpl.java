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
        if (!authService.validateSession(token)) {
            throw new RemoteException("Invalid session");
        }
    }

    private int getUserId(String token) throws RemoteException {
        Integer userId = authService.getUserIdFromToken(token);
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
    public List<Song> getSongsByName(String name, String token) throws RemoteException {
        validateSession(token);
        return songLibrary.getSongsByName(name);
    }

    @Override
    public List<Song> getLikedSongs(String token) throws RemoteException {
        int userId = getUserId(token);
        return songLibrary.getLikedSongs(userId);
    }

    @Override
    public byte[] streamSong(int songId) throws RemoteException {
        System.out.println("Streaming song with ID: " + songId);
        activeStreams.add(songId);
        try {
            byte[] songData = songLibrary.getSongData(songId);
            System.out.println("Successfully retrieved song data for ID: " + songId);
            return songData;
        } catch (Exception e) {
            System.err.println("Error streaming song " + songId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error streaming song: " + e.getMessage(), e);
        } finally {
            activeStreams.remove(songId);
            System.out.println("Stream ended for song ID: " + songId);
        }
    }

    @Override
    public synchronized void uploadSong(Song song, byte[] fileData, String artistName, String albumName) 
            throws RemoteException {
        try {
            System.out.println("Attempting to upload song: " + song.getTitle());
            System.out.println("Artist: " + artistName + ", Album: " + albumName);
            
            // if (songLibrary.getSongById(song.getId()) != null) {
            //     System.err.println("Error: Song already exists with ID: " + song.getId());
            //     throw new RemoteException("Song already exists with ID: " + song.getId());
            // }
            
            Artist artist = artistDAO.getArtistByName(artistName);
            Album album = albumDAO.getAlbumByName(albumName);
            
            if (artist == null) {
                System.out.println("Creating new artist: " + artistName);
                artist = new Artist(0, artistName, "", new ArrayList<>(), new ArrayList<>());
                int artistId = artistDAO.addArtist(artist);
                artist.setId(artistId);
                System.out.println("Created artist with ID: " + artistId);
            }
            
            if (album == null) {
                System.out.println("Creating new album: " + albumName);
                album = new Album(0, albumName, artist.getId(), "", null);
                int albumId = albumDAO.addAlbum(album);
                album.setId(albumId);
                System.out.println("Created album with ID: " + albumId);
            }
            
            song.setArtistId(artist.getId());
            song.setAlbumId(album.getId());
            System.out.println("Inserting song into database...");
            int songId = songDAO.insertSong(song);
            System.out.println("Song inserted with ID: " + songId);
            
            // Add song to album_items table
            System.out.println("Adding song to album items...");
            albumDAO.insertAlbumItems(album.getId(), songId);
                System.out.println("Successfully added song to album items");
            
            
            System.out.println("Storing song data...");
            fileManager.storeSongData(songId, fileData);
            System.out.println("Song data stored successfully");
            
            System.out.println("Refreshing song library...");
            songLibrary.refresh();
            System.out.println("Song upload completed successfully");
        } catch (SQLException e) {
            System.err.println("Database error during song upload: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error storing song in database: " + e.getMessage(), e);
        } catch (IOException e) {
            System.err.println("File system error during song upload: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error storing song data: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error during song upload: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Unexpected error during song upload: " + e.getMessage(), e);
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
            System.out.println("Inserting album: " + album.getTitle());
            int albumId = albumDAO.insertAlbum(album);
            System.out.println("Successfully inserted album with ID: " + albumId);
            return albumId;
        } catch (SQLException e) {
            System.err.println("Error inserting album: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error inserting album: " + e.getMessage(), e);
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
    public List<Album> getAlbumsByTitle(String title, String token) throws RemoteException {
        validateSession(token);
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
            System.out.println("Creating playlist '" + name + "' for user " + userId);
            Playlist playlist = playlistManager.createPlaylist(userId, name);
            System.out.println("Successfully created playlist with ID: " + playlist.getId());
            return playlist;
        } catch (SQLException e) {
            System.err.println("Error creating playlist for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error creating playlist: " + e.getMessage(), e);
        }
    }

    @Override
    public void addSongToPlaylist(int playlistId, int songId) throws RemoteException {
        try {
            System.out.println("Adding song " + songId + " to playlist " + playlistId);
            playlistManager.addSongToPlaylist(playlistId, songId);
            System.out.println("Successfully added song to playlist");
        } catch (SQLException e) {
            System.err.println("Error adding song " + songId + " to playlist " + playlistId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error adding song to playlist: " + e.getMessage(), e);
        }
    }
    @Override
    public void removeSongFromPlaylist(int playlistId, int songId) throws RemoteException {
        try {
            System.out.println("Removing song " + songId + " from playlist " + playlistId);
            playlistManager.removeSongFromPlaylist(playlistId, songId);
            System.out.println("Successfully removed song from playlist");
        } catch (SQLException e) {
            System.err.println("Error removing song " + songId + " from playlist " + playlistId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error removing song from playlist: " + e.getMessage(), e);
        }
    }
    @Override
    public void deletePlaylist(int playlistId) throws RemoteException {
        try {
            System.out.println("Deleting playlist " + playlistId);
            playlistManager.deletePlaylist(playlistId);
            System.out.println("Successfully deleted playlist");
        } catch (SQLException e) {
            System.err.println("Error deleting playlist " + playlistId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error deleting playlist: " + e.getMessage(), e);
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
            System.out.println("Recording play of song " + songId + " for user " + userId);
            historyDAO.addToHistory(userId, songId);
            System.out.println("Successfully recorded play");
        } catch (SQLException e) {
            System.err.println("Error recording play for user " + userId + " song " + songId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error recording play: " + e.getMessage(), e);
        }
    }

    @Override
    public void likeSong(int songId, String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            System.out.println("User " + userId + " liking song " + songId);
            songDAO.likeSong(userId, songId);
            System.out.println("Successfully liked song " + songId + " for user " + userId);
        } catch (SQLException e) {
            System.err.println("Error liking song " + songId + " for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error liking song: " + e.getMessage(), e);
        }
    }
    @Override
    public void unlikeSong(int songId, String token) throws RemoteException {
        int userId = getUserId(token);
        try {
            System.out.println("User " + userId + " unliking song " + songId);
            songDAO.unlikeSong(userId, songId);
            System.out.println("Successfully unliked song " + songId + " for user " + userId);
        } catch (SQLException e) {
            System.err.println("Error unliking song " + songId + " for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error unliking song: " + e.getMessage(), e);
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
            return authService.login(username, password);
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
        authService.logout(authToken);
    }

    @Override
    public boolean isLoggedIn(String authToken) throws RemoteException {
        return authService.isLoggedIn(authToken);
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
        authService.cleanupOldSessions();
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
            System.out.println("Clearing history for user " + userId);
            historyDAO.clearHistory(userId);
            System.out.println("Successfully cleared history");
        } catch (SQLException e) {
            System.err.println("Error clearing history for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error clearing history: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Song> getHistorySongs(String token) throws RemoteException {
        int userId = getUserId(token);
        return historyDAO.getHistorySongs(userId);
    }

    @Override
    public Boolean register(String username, String password, String email) throws RemoteException {
        try {
            String sessionToken = authService.register(username, password, email);
            return sessionToken != null;
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Database error during registration: " + e.getMessage(), e);
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
            System.out.println("Adding user: " + user.getUsername());
            userDAO.createUser(user);
            System.out.println("Successfully added user");
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error adding user: " + e.getMessage(), e);
        }
    }
    

    //Admin operations
    @Override
    public void addAdmin(int userId) throws RemoteException {
        try {
            System.out.println("Adding admin privileges for user " + userId);
            userDAO.addAdmin(userId);
            System.out.println("Successfully added admin privileges");
        } catch (SQLException e) {
            System.err.println("Error adding admin privileges: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error adding admin: " + e.getMessage(), e);
        }
    }
    @Override
    public void removeAdmin(int userId) throws RemoteException {
        try {
            System.out.println("Removing admin privileges for user " + userId);
            userDAO.removeAdmin(userId);
            System.out.println("Successfully removed admin privileges");
        } catch (SQLException e) {
            System.err.println("Error removing admin privileges: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error removing admin: " + e.getMessage(), e);
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
    public List<Artist> getArtistsByName(String name, String token) throws RemoteException {
        validateSession(token);
        try {
            return artistDAO.getArtistsByName(name);
        } catch (SQLException e) {
            throw new RemoteException("Error fetching artists by name", e);
        }
    }
    @Override
    public void addArtist(Artist artist) throws RemoteException {
        try {
            System.out.println("Adding artist: " + artist.getName());
            artistDAO.addArtist(artist);
            System.out.println("Successfully added artist with ID: " + artist.getId());
        } catch (SQLException e) {
            System.err.println("Error adding artist: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Error adding artist: " + e.getMessage(), e);
        }
    }

}
    