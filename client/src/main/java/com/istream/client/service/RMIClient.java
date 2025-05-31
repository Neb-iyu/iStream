package com.istream.client.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.PlayHistory;
import com.istream.model.Playlist;
import com.istream.model.Song;
import com.istream.model.User;

import javafx.scene.image.Image;

public interface RMIClient extends Remote {
    // Authentication methods
    String login(String username, String password) throws RemoteException;
    void logout() throws RemoteException;
    boolean isLoggedIn() throws RemoteException;
    Boolean register(String username, String password, String email) throws RemoteException;
    int getUserId();
    
    // User methods
    User getCurrentUser() throws RemoteException;
    
    // Song methods
    List<Song> getAllSongs() throws RemoteException;
    Song getSongById(int songId) throws RemoteException;
    List<Song> getSongsByAlbumId(int albumId) throws RemoteException;
    List<Song> getSongsByArtistId(int artistId) throws RemoteException;
    List<Song> getSongsByName(String name) throws RemoteException;
    List<Song> getLikedSongs() throws RemoteException;
    void likeSong(int songId) throws RemoteException;
    void unlikeSong(int songId) throws RemoteException;
    boolean isSongLiked(int songId) throws RemoteException;
    byte[] streamSong(int songId) throws RemoteException;
    void uploadSong(Song song, byte[] fileData, String artistName, String albumName) throws RemoteException;
    List<Song> searchSongs(String query) throws RemoteException;
    List<Song> getHistorySongs() throws RemoteException;
    
    // Album methods
    List<Album> getAllAlbums() throws RemoteException;
    Album getAlbumById(int albumId) throws RemoteException;
    List<Album> getAlbumsByArtist(int artistId) throws RemoteException;
    List<Album> getAlbumsByTitle(String title) throws RemoteException;
    
    // Artist methods
    List<Artist> getAllArtists() throws RemoteException;
    Artist getArtistById(int artistId) throws RemoteException;
    List<Artist> getArtistsByName(String name) throws RemoteException;
    
    // Playlist methods
    List<Playlist> getUserPlaylists() throws RemoteException;
    Playlist createPlaylist(String name) throws RemoteException;
    void addSongToPlaylist(int playlistId, int songId) throws RemoteException;
    void removeSongFromPlaylist(int playlistId, int songId) throws RemoteException;
    void deletePlaylist(int playlistId) throws RemoteException;
    Playlist getPlaylist(int playlistId) throws RemoteException;
    List<Song> getSongsByPlaylistId(int playlistId) throws RemoteException;
    
    // History methods
    void recordPlay(int songId) throws RemoteException;
    // List<PlayHistory> getHistory() throws RemoteException;

    void clearHistory() throws RemoteException;
    
    // Admin methods
    void addAdmin(int userId) throws RemoteException;
    void removeAdmin(int userId) throws RemoteException;
    boolean isAdmin(int userId) throws RemoteException;
    
    // Dummy data methods
    void addArtist(Artist artist) throws RemoteException;
    void addAlbum(Album album) throws RemoteException;
    void addSong(Song song) throws RemoteException;
    void addUser(User user) throws RemoteException;
    void addPlaylist(Playlist playlist) throws RemoteException;

    //Image methods
    Image getImage(String path) throws RemoteException;
    void saveImage(String path, Image image) throws RemoteException;
}