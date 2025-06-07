package com.istream.concurrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.istream.database.DatabaseManager;
import com.istream.database.dao.SongDAO;
import com.istream.database.dao.AlbumDAO;
import com.istream.database.dao.ArtistDAO;
import com.istream.model.Song;
import com.istream.model.Album;
import com.istream.model.Artist;

public class SongLibrary {
    private static final int PAGE_SIZE = 50;
    private static final int MAX_CACHE_SIZE = 1000;
    private static final int MAX_MEMORY_CACHE_SIZE = 100;
    
    private final DatabaseManager dbManager;
    private final SongDAO songDAO;
    private final AlbumDAO albumDAO;
    private final ArtistDAO artistDAO;
    private final String libraryPath;
    private final ConcurrentHashMap<Integer, Song> songCache;
    private final ConcurrentHashMap<Integer, byte[]> songDataCache;
    private final AtomicInteger nextId;
    private List<Song> songs;
    
    public SongLibrary(DatabaseManager dbManager, String libraryPath) throws SQLException {
        this.dbManager = dbManager;
        this.songDAO = dbManager.getSongDAO();
        this.albumDAO = dbManager.getAlbumDAO();
        this.artistDAO = dbManager.getArtistDAO();
        this.libraryPath = libraryPath;
        this.songCache = new ConcurrentHashMap<>();
        this.songDataCache = new ConcurrentHashMap<>();
        this.nextId = new AtomicInteger(1);
        this.songs = new ArrayList<>();
        
        initializeLibrary();
    }
    
    private void initializeLibrary() throws SQLException {
        songs = songDAO.getAllSongs();
        
        try {
            Path path = Paths.get(libraryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                return;
            }
            
            Files.walk(path)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".mp3"))
                .forEach(this::addSongFromFile);
                
        } catch (IOException e) {
            throw new SQLException("Error initializing song library", e);
        }
    }
    
    private void addSongFromFile(Path filePath) {
        try {
            String fileName = filePath.getFileName().toString();
            System.out.println("Processing file: " + fileName);
            String title = fileName.substring(0, fileName.lastIndexOf('.'));
            final int id;
            try {
                id = Integer.parseInt(title);
            }
            catch (NumberFormatException e) {
                System.err.println("Invalid song ID in file name: " + fileName);
                return;
            }
            if (songs.stream().noneMatch(s -> s.getId() == id)) {
                // Create default artist and album
                Artist artist = artistDAO.getArtistByName("Unknown Artist");
                if (artist == null) {
                    artist = new Artist(0, "Unknown Artist", "", new ArrayList<>(), new ArrayList<>());
                    int artistId = artistDAO.addArtist(artist);
                    artist.setId(artistId);
                }
                
                Album album = albumDAO.getAlbumByName("Unknown Album");
                if (album == null) {
                    album = new Album(0, "Unknown Album", artist.getId(), "", new ArrayList<>());
                    int albumId = albumDAO.addAlbum(album);
                    album.setId(albumId);
                }
                
                // Create song with proper IDs
                Song song = new Song(
                    nextId.getAndIncrement(),
                    title,
                    artist.getId(),
                    album.getId(),
                    "Unknown Genre",
                    0,
                    " ", // filePath will be set after insertion
                    null,
                    0
                );
                
                try {
                    // Insert song to get its ID
                    int songId = songDAO.insertSong(song);
                    song.setId(songId);
                    
                    // Move file to proper location with song ID
                    String newFileName = songId + ".mp3";
                    Path newPath = Paths.get(libraryPath, newFileName);
                    Files.move(filePath, newPath);
                    
                    // Update song with new file path
                    song.setFilePath(newPath.toString());
                    songDAO.updateSong(song);
                    
                    songs.add(song);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void refresh() throws SQLException {
        songs = songDAO.getAllSongs();
        clearCache();
    }
    
    public List<Song> getAllSongs() {
        return Collections.unmodifiableList(songs);
    }
    
    public List<Song> getSongs(int page) {
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, songs.size());
        
        if (start >= songs.size()) {
            return new ArrayList<>();
        }
        
        return songs.subList(start, end).stream()
            .map(song -> {
                if (!songCache.containsKey(song.getId())) {
                    if (songCache.size() >= MAX_CACHE_SIZE) {
                        songCache.keySet().stream()
                            .findFirst()
                            .ifPresent(songCache::remove);
                    }
                    songCache.put(song.getId(), song);
                }
                return songCache.get(song.getId());
            })
            .collect(Collectors.toList());
    }
    
    public Song getSongById(int id) {
        return songCache.computeIfAbsent(id, k -> 
            songs.stream()
                .filter(s -> s.getId() == k)
                .findFirst()
                .orElse(null)
        );
    }
    
    public List<Song> getSongsByAlbumId(int albumId) {
        try {
            return songDAO.getSongsByAlbum(albumId);
        } catch (SQLException e) {
            throw new RuntimeException("Error getting songs by album ID", e);
        }
    }
    
    public List<Song> getSongsByArtistId(int artistId) {
        try {
            return songDAO.getSongsByArtist(artistId);
        } catch (SQLException e) {
            throw new RuntimeException("Error getting songs by artist ID", e);
        }
    }
    
    public List<Song> getSongsByName(String name) {
        return songs.stream()
            .filter(s -> s.getTitle().toLowerCase().contains(name.toLowerCase()))
            .limit(PAGE_SIZE)
            .collect(Collectors.toList());
    }
    
    public List<Song> getLikedSongs(int userId) {
        try {
            return songDAO.getLikedSongs(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Error getting liked songs", e);
        }
    }
    
    public List<Song> searchSongs(String query) {
        try {
            return songDAO.searchSongs(query);
        } catch (SQLException e) {
            throw new RuntimeException("Error searching songs", e);
        }
    }
    
    public byte[] getSongData(int songId) {
        return songDataCache.computeIfAbsent(songId, k -> {
            try {
                Path songPath = Paths.get(libraryPath, songId + ".mp3");
                if (!Files.exists(songPath)) {
                    return null;
                }
                byte[] data = Files.readAllBytes(songPath);
                if (songDataCache.size() >= MAX_MEMORY_CACHE_SIZE) {
                    songDataCache.keySet().stream()
                        .findFirst()
                        .ifPresent(songDataCache::remove);
                }
                return data;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }
    
    public int getTotalPages() {
        return (int) Math.ceil((double) songs.size() / PAGE_SIZE);
    }
    
    public void clearCache() {
        songCache.clear();
        songDataCache.clear();
    }
}