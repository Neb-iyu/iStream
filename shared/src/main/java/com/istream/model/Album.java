package com.istream.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Album implements Serializable {
    private int id;
    private String title;
    private int artistId;
    private String coverArtPath;
    private List<Song> songs;

    public Album() {
        this.songs = new ArrayList<>();
    }

    public Album(int id, String title, int artistId, String coverArtPath, List<Song> songs) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.coverArtPath = coverArtPath;
        this.songs = (songs != null) ? songs : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getCoverArtPath() {
        return coverArtPath;
    }

    public void setCoverArtPath(String coverArtPath) {
        this.coverArtPath = coverArtPath;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
