package com.istream.model;

import java.util.*;
public class Playlist {
    private int id;
    
    private String name;
    private String description;

    private User owner; // User who created the playlist
    private HashSet<Song> songs; // List of songs in the playlist
    private int playCount; // Number of times the playlist has been played

    public Playlist(int id, String name, String description, User owner, HashSet<Song> songs, int playCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.songs = new HashSet<>(songs);
        this.playCount = playCount;
    }
    public void addSong(Song song){
        songs.add(song);
    
    }
    public void removeSong(Song song){
        songs.remove(song);
    }
    public int getId() {
        return id;
    }
    public void incrementPlayCount() {
        this.playCount++;
    }
    
}
