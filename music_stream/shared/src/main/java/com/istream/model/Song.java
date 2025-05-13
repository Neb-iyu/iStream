package com.istream.model;

public class Song {
    private int id;

    private String title;
    private String artist;
    private String album;
    private String genre;
    private int duration; // in seconds
    private String filePath; // path to the song file on the server
    private String coverArtPath; // path to the cover art image
    private int year;
    private int playCount; // number of times the song has been played

    public Song(int id, String title, String artist, String album, String genre, int duration, String filePath, String coverArtPath, int year) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.duration = duration;
        this.filePath = filePath;
        this.coverArtPath = coverArtPath;
        this.year = year;
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
    public String getAlbum() {
        return album;
    }
    public int getDuration() {
        return duration;
    }
    public String getFilePath() {
        return filePath;
    }
    public String getCoverArtPath() {
        return coverArtPath;
    }
}
