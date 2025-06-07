package com.istream.client.service;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import com.istream.client.util.ImageConverter;
import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.Playlist;
import com.istream.model.Song;
import com.istream.model.User;
import com.istream.rmi.MusicService;

import javafx.scene.image.Image;

public class RMIClientImpl implements RMIClient {
    private final MusicService musicService;
    private String sessionToken;
    private int userId;
    
    public RMIClientImpl(String host, int port) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        this.musicService = (MusicService) registry.lookup("MusicService");
        this.userId = -1; // Initialize with invalid userId
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public User getCurrentUser() throws RemoteException {
        return musicService.getUserById(sessionToken);
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        this.sessionToken = musicService.login(username, password);
        return sessionToken;
    }

    @Override
    public void logout() throws RemoteException {
        musicService.logout(sessionToken);
    }

    @Override
    public boolean isLoggedIn() throws RemoteException {
        return musicService.isLoggedIn(sessionToken);
    }

    @Override
    public Boolean register(String username, String password, String email) throws RemoteException {
        return musicService.register(username, password, email);
    }

    @Override
    public void recordPlay(int songId) throws RemoteException {
        musicService.recordPlay(songId, sessionToken);
    }
    
    @Override
    public byte[] streamSong(int songId) throws RemoteException {
        try {
            return musicService.streamSong(songId);
        } catch (RemoteException e) {
            throw new RemoteException("Error streaming song", e);
        }
    }
    
    @Override
    public List<Song> getAllSongs() throws RemoteException {
        return musicService.getAllSongs(sessionToken);
    }
    
    @Override
    public Song getSongById(int songId) throws RemoteException {
        return musicService.getSongById(songId, sessionToken);
    }
    

    // public List<PlayHistory> getHistory() throws RemoteException {
    //     return musicService.getHistory(sessionToken);
    // }
    
    @Override
    public List<Song> getLikedSongs() throws RemoteException {
        return musicService.getLikedSongs(sessionToken);
    }
    
    @Override
    public List<Song> getSongsByAlbumId(int albumId) throws RemoteException {
        return musicService.getSongsByAlbumId(albumId, sessionToken);
    }
    
    @Override
    public List<Song> getSongsByArtistId(int artistId) throws RemoteException {
        return musicService.getSongsByArtistId(artistId, sessionToken);
    }
    
    @Override
    public List<Song> getSongsByPlaylistId(int playlistId) throws RemoteException {
        return musicService.getSongsByPlaylistId(playlistId);
    }

    @Override
    public List<Song> getSongsByName(String name) throws RemoteException {
        return musicService.getSongsByName(name, sessionToken);
    }
    
    @Override
    public List<Artist> getAllArtists() throws RemoteException {
        return musicService.getAllArtists(sessionToken);
    }
    
    @Override
    public Artist getArtistById(int artistId) throws RemoteException {
        return musicService.getArtistById(artistId, sessionToken);
    }
    
    @Override
    public List<Artist> getArtistsByName(String name) throws RemoteException {
        return musicService.getArtistsByName(name, sessionToken);
    }
    
    @Override
    public List<Album> getAllAlbums() throws RemoteException {
        return musicService.getAllAlbums(sessionToken);
    }
    
    @Override
    public List<Album> getAlbumsByTitle(String title) throws RemoteException {
        return musicService.getAlbumsByTitle(title, sessionToken);
    }
    
    @Override
    public Album getAlbumById(int albumId) throws RemoteException {
        return musicService.getAlbumById(albumId, sessionToken);
    }
    
    @Override
    public List<Album> getAlbumsByArtist(int artistId) throws RemoteException {
        return musicService.getAlbumsByArtist(artistId, sessionToken);
    }
    
    @Override
    public List<Playlist> getUserPlaylists() throws RemoteException {
        return musicService.getUserPlaylists(sessionToken);
    }
    
    @Override
    public Playlist createPlaylist(String name) throws RemoteException {
        return musicService.createPlaylist(sessionToken, name);
    }
    
    @Override
    public void addSongToPlaylist(int playlistId, int songId) throws RemoteException {
        musicService.addSongToPlaylist(playlistId, songId);
    }
    
    @Override
    public void removeSongFromPlaylist(int playlistId, int songId) throws RemoteException {
       // TODO: Implement
        musicService.removeSongFromPlaylist(playlistId, songId);
    }
    
    @Override
    public void deletePlaylist(int playlistId) throws RemoteException {
        // TODO: Implement
        musicService.deletePlaylist(playlistId);
    }

    @Override
    public Playlist getPlaylist(int playlistId) throws RemoteException {
        return musicService.getPlaylist(playlistId);
    }
    
    @Override
    public void likeSong(int songId) throws RemoteException {
        try {
            musicService.likeSong(songId, sessionToken);
        } catch (RemoteException e) {
            throw new RemoteException("Error liking song", e);
        }
    }
    
    @Override
    public void unlikeSong(int songId) throws RemoteException {
        try {
            musicService.unlikeSong(songId, sessionToken);
        } catch (RemoteException e) {
            throw new RemoteException("Error unliking song", e);
        }
    }
    
    @Override
    public boolean isSongLiked(int songId) throws RemoteException {
        try {
            return musicService.isSongLiked(songId, sessionToken);
        } catch (RemoteException e) {
            throw new RemoteException("Error checking if song is liked", e);
        }
    }
    
    @Override
    public void uploadSong(Song song, byte[] fileData, String artistName, String albumName) throws RemoteException {
        musicService.uploadSong(song, fileData, artistName, albumName);
    }

    @Override
    public void addArtist(Artist artist) throws RemoteException {
        musicService.addArtist(artist);
    }

    @Override
    public void addAlbum(Album album) throws RemoteException {
        int albumId = musicService.insertAlbum(album);
        System.out.println("Added album: " + album.getTitle() + " with ID: " + albumId);
    }

    @Override
    public void addSong(Song song) throws RemoteException {
        // Create a dummy byte array for the song data
        byte[] dummyData = new byte[1024]; // 1KB dummy data
        musicService.uploadSong(song, dummyData, "Unknown Artist", "Unknown Album");
        System.out.println("Added song: " + song.getTitle());
    }

    @Override
    public void addUser(User user) throws RemoteException {
        musicService.addUser(user);
    }
    
    @Override
    public void addPlaylist(Playlist playlist) throws RemoteException {
        // Implementation not needed as it's handled by createPlaylist
    }

    @Override
    public Image getImage(String path) throws RemoteException {
        try {
            //System.out.println("Getting image: " + path);
            byte[] imageData = musicService.getImage(path);
            return ImageConverter.bytesToImage(imageData);
        }
        catch (Exception e) {
            //System.out.println("Error getting image: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void saveImage(String path, Image image) throws RemoteException {
        try {   
            byte[] imageData = ImageConverter.imageToBytes(image);
            musicService.saveImage(path, imageData);
        }
        catch (Exception e) {
            System.out.println("Error saving image: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isAdmin(int userId) throws RemoteException {
        return musicService.isAdmin(userId);
    }

    @Override
    public void addAdmin(int userId) throws RemoteException {
        musicService.addAdmin(userId);
    }

    @Override
    public void removeAdmin(int userId) throws RemoteException {
        musicService.removeAdmin(userId);
    }
    
    @Override
    public List<Song> searchSongs(String query) throws RemoteException {
        return musicService.searchSongs(query, sessionToken);
    }

    @Override
    public List<Song> getHistorySongs() throws RemoteException {
        try {
            return musicService.getHistorySongs(sessionToken);
        } catch (RemoteException e) {
            System.err.println("Error getting history songs: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }

    @Override
    public void clearHistory() throws RemoteException {
        musicService.clearHistory(sessionToken);
    }
}