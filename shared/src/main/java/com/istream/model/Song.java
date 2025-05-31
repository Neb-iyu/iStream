package com.istream.model;

import java.io.Serializable;

public class Song implements Serializable {
    private int id;
    private String title;
    private int artistId;
    private int albumId;
    private String genre;
    private int duration; // in seconds
    private String filePath; // path to the song file on the server
    private String coverArtPath; // path to the cover art image
    private int year;
    private int playCount; // number of times the song has been played

    public Song() {
        // Default constructor for serialization
    }

    public Song(int id, String title, int artistId, int albumId, String genre, int duration, String filePath, String coverArtPath, int year) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.albumId = albumId;
        this.genre = genre;
        this.duration = duration;
        this.filePath = filePath;
        this.coverArtPath = coverArtPath;
        this.year = year;
        this.playCount = 0;
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

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCoverArtPath() {
        return coverArtPath;
    }

    public void setCoverArtPath(String coverArtPath) {
        this.coverArtPath = coverArtPath;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }
}
