package com.istream.client.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.istream.model.Song;

public interface RMIClient extends Remote {
    List<Song> getAllSongs() throws RemoteException;
    byte[] streamSong(int songId) throws RemoteException;
    void uploadSong(Song song, byte[] fileData) throws RemoteException;
}