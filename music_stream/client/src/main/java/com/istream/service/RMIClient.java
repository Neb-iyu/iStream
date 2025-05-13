package com.istream.service;

import com.istream.rmi.MusicService;
import com.istream.model.Song;

import java.io.InputStream;
import java.util.List;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.Naming;

public class RMIClient {
    private final MusicService musicService;
    private String sessionToken;
    
    public RMIClient(String host, int port) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        this.musicService = (MusicService) registry.lookup("MusicService");
    }
    
    public boolean login(String username, String password) throws RemoteException {
        this.sessionToken = musicService.login(username, password);
        return sessionToken != null;
    }
    
    public List<Song> getAllSongs() throws RemoteException {
        // return musicService.getAllSongs(sessionToken);
        return musicService.getAllSongs();
    }
    
    public byte[] streamSong(int songId) throws RemoteException {
        return musicService.streamSong(songId);
        // return musicService.streamSong(sessionToken, songId);
    }
    
    // Other wrapped methods...
}