package com.istream.model;

public class PlayHistory {
    private int userId;
    private int songId;
    private String timestamp;

    public PlayHistory(int userId, int songId, String timestamp) {
        this.userId = userId;
        this.songId = songId;
        this.timestamp = timestamp;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
