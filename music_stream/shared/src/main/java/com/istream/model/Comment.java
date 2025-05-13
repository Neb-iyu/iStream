package com.istream.model;

public class Comment {
    private int id;
    private String content; // The content of the comment
    private User user; // The user who made the comment
    private Song song; // The song on which the comment was made
    private Playlist playlist; // The playlist on which the comment was made
    private String timestamp; // Timestamp of when the comment was made

    public Comment(int id, String content, User user, Song song, Playlist playlist, String timestamp) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.song = song;
        this.playlist = playlist;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public User getUser() {
        return user;
    }

    public Song getSong() {
        return song;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public String getTimestamp() {
        return timestamp;
    }
    
}
