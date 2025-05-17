package com.istream.model;
import java.util.List;

public class Album {
    private int id;
    private String title;
    private String artist;
    private String coverArtPath;
    private List<Song> songs;

    public Album(int id, String title, String artist, String coverArtPath, List<Song> songs) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.coverArtPath = coverArtPath;
        this.songs = songs;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
    public String getArtist() {
        return artist;
    }

    public String getCoverArtPath() {
        return coverArtPath;
    }
    public List<Song> getSongs() {
        return songs;
    }
    
}
