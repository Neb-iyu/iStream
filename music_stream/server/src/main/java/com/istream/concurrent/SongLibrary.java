package com.istream.concurrent;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.istream.model.*;
import com.istream.database.DatabaseManager;

public class SongLibrary {
    private final DatabaseManager dbManager;
    private final List<Song> songs = new CopyOnWriteArrayList<>();
    
    public SongLibrary(DatabaseManager dbManager) throws SQLException { 
        this.dbManager = dbManager;
        refresh();
    }
    
    public synchronized void refresh() throws SQLException {
        songs.clear();
        songs.addAll(dbManager.getAllSongs());
    }
    
    public List<Song> getAllSongs() {
        return Collections.unmodifiableList(songs);
    }
    
    public Song getSongById(int id) {
        return songs.stream()
            .filter(s -> s.getId() == id)
            .findFirst()
            .orElse(null);
    }
}