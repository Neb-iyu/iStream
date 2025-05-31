package com.istream.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Comment implements Serializable {
    private int id;
    private String content; // The content of the comment
    private int userId;
    private Integer songId;
    private Integer playlistId;
    private LocalDateTime timestamp;

    public Comment() {
        this.timestamp = LocalDateTime.now();
    }

    public Comment(int id, String content, int userId, Integer songId, Integer playlistId, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.userId = userId;
        this.songId = songId;
        this.playlistId = playlistId;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getSongId() {
        return songId;
    }

    public void setSongId(Integer songId) {
        this.songId = songId;
    }

    public Integer getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(Integer playlistId) {
        this.playlistId = playlistId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
