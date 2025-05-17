package com.istream.client.service;

import com.istream.model.Song;
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
    
    public boolean login(String username, String password) throws RemoteException {
        this.sessionToken = musicService.login(username, password);
        return sessionToken != null;
    }
    
    @Override
    public List<Song> getAllSongs() throws RemoteException {
        return musicService.getAllSongs();
    }
    
    @Override
    public byte[] streamSong(int songId) throws RemoteException {
        return musicService.streamSong(songId);
    }

    @Override
    public void uploadSong(Song song, byte[] fileData) throws RemoteException {
        musicService.uploadSong(song, fileData);
    }
} 