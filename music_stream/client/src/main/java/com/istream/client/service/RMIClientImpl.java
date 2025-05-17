package com.istream.client.service;

import com.istream.model.*;
import com.istream.rmi.MusicService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.List;

public class RMIClientImpl implements RMIClient {
    private final MusicService musicService;
    private String sessionToken;
    
    public RMIClientImpl(String host, int port) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        this.musicService = (MusicService) registry.lookup("MusicService");
    }
    
    @Override
    public String login(String username, String password) throws RemoteException {
        this.sessionToken = musicService.login(username, password);
        return sessionToken;
    }
    
    @Override
    public List<Song> getAllSongs() throws RemoteException {
        return musicService.getAllSongs();
    }
    
    @Override
    public Song getSongById(int id) throws RemoteException {
        return musicService.getSongById(id);
    }
    
    @Override
    public byte[] streamSong(int songId) throws RemoteException {
        return musicService.streamSong(songId);
    }

    @Override
    public List<Album> getAlbumsByArtist(String artist) throws RemoteException {
        return musicService.getAlbumsByArtist(artist);
    }

    @Override
    public List<Album> getAlbumsByTitle(String title) throws RemoteException {
        return musicService.getAlbumsByTitle(title);
    }

    @Override
    public List<Album> getAllAlbums() throws RemoteException {
        return musicService.getAllAlbums();
    }

    @Override
    public List<Playlist> getUserPlaylists(int userId) throws RemoteException {
        return musicService.getUserPlaylists(userId);
    }

    @Override
    public Playlist createPlaylist(int userId, String name) throws RemoteException {
        return musicService.createPlaylist(userId, name);
    }

    @Override
    public void addSongToPlaylist(int playlistId, int songId) throws RemoteException {
        musicService.addSongToPlaylist(playlistId, songId);
    }

    @Override
    public List<Song> getSongsByPlaylistId(int playlistId) throws RemoteException {
        return musicService.getSongsByPlaylistId(playlistId);
    }

    @Override
    public void recordPlay(int userId, int songId) throws RemoteException {
        musicService.recordPlay(userId, songId);
    }

    @Override
    public void likeSong(int userId, int songId) throws RemoteException {
        musicService.likeSong(userId, songId);
    }

    @Override
    public List<PlayHistory> getHistory(int userId) throws RemoteException {
        return musicService.getHistory(userId);
    }

    @Override
    public List<Song> getLikedSongs(int userId) throws RemoteException {
        return musicService.getLikedSongs(userId);
    }

    @Override
    public List<Artist> getAllArtists() throws RemoteException {
        return musicService.getAllArtists();
    }

    @Override
    public Artist getArtistById(int id) throws RemoteException {
        return musicService.getArtistById(id);
    }

    @Override
    public List<Song> getSongsByArtist(int artistId) throws RemoteException {
        return musicService.getSongsByArtist(artistId);
    }

    @Override
    public List<Song> getSongsByArtistId(int artistId) throws RemoteException {
        return musicService.getSongsByArtistId(artistId);
    }

    @Override
    public void logout(String authToken) throws RemoteException {
        musicService.logout(authToken);
    }

    @Override
    public boolean isLoggedIn(String authToken) throws RemoteException {
        return musicService.isLoggedIn(authToken);
    }

    @Override
    public Boolean register(String username, String password, String email) throws RemoteException {
        return musicService.register(username, password, email);
    }
} 