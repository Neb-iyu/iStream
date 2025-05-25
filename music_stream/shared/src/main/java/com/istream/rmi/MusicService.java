package com.istream.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.PlayHistory;
import com.istream.model.Playlist;
import com.istream.model.Song;
import com.istream.model.User;

public interface MusicService extends Remote {
    // Song operations
    List<Song> getAllSongs(String token) throws RemoteException;
    Song getSongById(int id, String token) throws RemoteException;
    byte[] streamSong(int songId) throws RemoteException;
    List<Song> getSongsByAlbumId(int albumId, String token) throws RemoteException;
    List<Song> getSongsByArtistId(int artistId, String token) throws RemoteException;
    List<Song> getSongsByName(String name) throws RemoteException;
    List<Song> getLikedSongs(String token) throws RemoteException;
    List<Song> searchSongs(String query, String token) throws RemoteException;
    void clearHistory(String token) throws RemoteException;
    List<Song> getHistorySongs(String token) throws RemoteException;
    // Album operations
    int insertAlbum(Album album) throws RemoteException;
    List<Album> getAlbumsByArtist(int artistId, String token) throws RemoteException;
    List<Album> getAlbumsByTitle(String title) throws RemoteException;
    Album getAlbumById(int id, String token) throws RemoteException;
    List<Album> getAllAlbums(String token) throws RemoteException;
    List<Album> searchAlbums(String query, String token) throws RemoteException;
    // Playlist operations
    List<Playlist> getUserPlaylists(String token) throws RemoteException;
    Playlist createPlaylist(String token, String name) throws RemoteException;
    void addSongToPlaylist(int playlistId, int songId) throws RemoteException;
    void removeSongFromPlaylist(int playlistId, int songId) throws RemoteException;
    void deletePlaylist(int playlistId) throws RemoteException;
    Playlist getPlaylist(int playlistId) throws RemoteException;
    List<Song> getSongsByPlaylistId(int playlistId) throws RemoteException;
    
    // User actions
    void recordPlay(int songId, String token) throws RemoteException;
    void likeSong(int songId, String token) throws RemoteException;
    void unlikeSong(int songId, String token) throws RemoteException;
    boolean isSongLiked(int songId, String token) throws RemoteException;
    List<PlayHistory> getHistory(String token) throws RemoteException;
    Boolean register(String username, String password, String email) throws RemoteException;
    User getUserById(String token) throws RemoteException;
    void addUser(User user) throws RemoteException;
    //void deleteUser(int userId) throws RemoteException;

    //Admin
    void addAdmin(int userId) throws RemoteException;
    void removeAdmin(int userId) throws RemoteException;
    boolean isAdmin(int userId) throws RemoteException;

    // Artist operations
    List<Artist> getAllArtists(String token) throws RemoteException;
    Artist getArtistById(int id, String token) throws RemoteException;
    List<Artist> getArtistsByName(String name) throws RemoteException;
    void addArtist(Artist artist) throws RemoteException;
    List<Artist> searchArtists(String query, String token) throws RemoteException;
    
    // Upload
    void uploadSong(Song song, byte[] fileData, String artistName, String albumName) throws RemoteException;

    //Authentication
    String login(String username, String password) throws RemoteException;
    void logout(String authToken) throws RemoteException;
    boolean isLoggedIn(String authToken) throws RemoteException;

    //Image methods
    byte[] getImage(String path) throws RemoteException;
    void saveImage(String path, byte[] image) throws RemoteException;
}
