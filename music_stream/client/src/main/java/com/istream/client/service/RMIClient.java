package com.istream.client.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.istream.model.*;

public interface RMIClient extends Remote {
    // Song operations
    List<Song> getAllSongs() throws RemoteException;
    Song getSongById(int id) throws RemoteException;
    byte[] streamSong(int songId) throws RemoteException;
    
    // Album operations
    List<Album> getAlbumsByArtist(String artist) throws RemoteException;
    List<Album> getAlbumsByTitle(String title) throws RemoteException;
    List<Album> getAllAlbums() throws RemoteException;
    
    // Playlist operations
    List<Playlist> getUserPlaylists(int userId) throws RemoteException;
    Playlist createPlaylist(int userId, String name) throws RemoteException;
    void addSongToPlaylist(int playlistId, int songId) throws RemoteException;
    List<Song> getSongsByPlaylistId(int playlistId) throws RemoteException;
    
    // User actions
    void recordPlay(int userId, int songId) throws RemoteException;
    void likeSong(int userId, int songId) throws RemoteException;
    List<PlayHistory> getHistory(int userId) throws RemoteException;
    List<Song> getLikedSongs(int userId) throws RemoteException;
    
    // Artist operations
    List<Artist> getAllArtists() throws RemoteException;
    Artist getArtistById(int id) throws RemoteException;
    List<Song> getSongsByArtist(int artistId) throws RemoteException;
    List<Song> getSongsByArtistId(int artistId) throws RemoteException;
    
    // Authentication
    String login(String username, String password) throws RemoteException;
    void logout(String authToken) throws RemoteException;
    boolean isLoggedIn(String authToken) throws RemoteException;
    Boolean register(String username, String password, String email) throws RemoteException;
}