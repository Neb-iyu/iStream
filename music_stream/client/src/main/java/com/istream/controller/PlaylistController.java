package com.istream.controller;

import com.istream.rmi.MusicService;

import com.istream.controller.AuthController;
import com.istream.model.Playlist;
import com.istream.model.Song;
import com.istream.util.SessionHolder;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;


public class PlaylistController {
    private final MusicService musicService;
    private final int userId;
    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private final ObservableList<Song> availableSongs = FXCollections.observableArrayList();

    public PlaylistController(MusicService musicService, int userId) {
        this.musicService = musicService;
        this.userId = userId;
        loadData();
    }

    private void loadData() {
        loadPlaylists();
        loadAvailableSongs();
    }

    private void loadPlaylists() {
        Task<List<Playlist>> task = new Task<>() {
            @Override
            protected List<Playlist> call() throws Exception {
                return musicService.getUserPlaylists(userId);
            }
        };
        
        task.setOnSucceeded(e -> playlists.setAll(task.getValue()));
        new Thread(task).start();
    }

    private void loadAvailableSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return musicService.getAllSongs();
            }
        };
        
        task.setOnSucceeded(e -> availableSongs.setAll(task.getValue()));
        new Thread(task).start();
    }

    public void addToPlaylist(int playlistId, int songId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                musicService.addSongToPlaylist(playlistId, songId);
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            loadPlaylists(); // Refresh playlist data
            showSuccessAlert("Song added to playlist");
        });
        
        task.setOnFailed(e -> showErrorAlert("Failed to add song"));
        
        new Thread(task).start();
    }

    public void createPlaylist(String name) {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return musicService.createPlaylist(userId, name);
            }
        };
        
        task.setOnSucceeded(e -> {
            loadPlaylists(); // Refresh list
            showSuccess("Playlist created");
        });
        
        new Thread(task).start();
    }
    
    public void showSuccessAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}