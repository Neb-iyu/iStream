package com.istream.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Artist implements Serializable {
    private int id;
    private String name;
    private String imageUrl;
    private List<Song> songs;
    private List<Album> albums;

    public Artist() {
        this.songs = new ArrayList<>();
        this.albums = new ArrayList<>();
    }

    public Artist(int id, String name, String imageUrl, List<Song> songs, List<Album> albums) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.songs = (songs != null) ? songs : new ArrayList<>();
        this.albums = (albums != null) ? albums : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }
}
