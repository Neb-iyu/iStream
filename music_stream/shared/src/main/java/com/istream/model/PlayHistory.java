package com.istream.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PlayHistory implements Serializable {
    private int id;
    private int userId;
    private int songId;
    private LocalDateTime timestamp;

    public PlayHistory() {
        this.timestamp = LocalDateTime.now();
    }

    public PlayHistory(int id, int userId, int songId, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.songId = songId;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
