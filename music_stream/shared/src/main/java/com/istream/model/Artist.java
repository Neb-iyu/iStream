package com.istream.model;

import java.util.List;

public class Artist {
    private int id;
    private String name;
    private String imageUrl;
    private List<Song> songs;
    private List<Album> albums;

    public Artist(int id, String name, String imageUrl, List<Song> songs, List<Album> albums) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.songs = songs;
        this.albums = albums;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }



    public List<Song> getSongs() {
        return songs;
    }

    public List<Album> getAlbums() {
        return albums;
    }
}
