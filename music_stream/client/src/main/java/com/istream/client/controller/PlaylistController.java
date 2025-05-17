package com.istream.client.controller;

import com.istream.rmi.MusicService;
import com.istream.client.util.SessionHolder;
import com.istream.model.Playlist;
import com.istream.model.Song;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

public class PlaylistController {
    @FXML private ListView<Playlist> playlistListView;
    @FXML private Button newPlaylistButton;

    private MusicService musicService;
    private int userId;
    private ObservableList<Playlist> playlists = FXCollections.observableArrayList();

    public PlaylistController() {}

    public void initServices(MusicService musicService, int userId) {
        this.musicService = musicService;
        this.userId = userId;
        if (playlistListView != null && this.musicService != null) {
            loadPlaylists();
        } else if (this.musicService != null) {
            // if initialize() hasn't run yet, loadPlaylists will be called by it later.
        }
    }

    @FXML
    public void initialize() {
        if (playlistListView != null) {
            playlistListView.setItems(playlists);
        }
        if (musicService != null) {
            loadPlaylists();
        }
        if (newPlaylistButton != null) {
            newPlaylistButton.setOnAction(event -> handleNewPlaylistClick());
        }
    }

    private void loadPlaylists() {
        Task<List<Playlist>> task = new Task<>() {
            @Override
            protected List<Playlist> call() throws Exception {
                try {
                    return musicService.getUserPlaylists(userId);
                } catch (RemoteException e) {
                    throw new Exception("Failed to load playlists: " + e.getMessage(), e);
                }
            }
        };
        
        task.setOnSucceeded(e -> playlists.setAll(task.getValue()));
        task.setOnFailed(e -> {
            if (task.getException() != null) {
                showErrorAlert("Error loading playlists: " + task.getException().getMessage());
            } else {
                showErrorAlert("Error loading playlists: Unknown error.");
            }
        });
        new Thread(task).start();
    }

    public ObservableList<Playlist> getPlaylists() {
        return playlists;
    }

    public void handleNewPlaylistClick() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Create a new playlist");
        dialog.setContentText("Enter playlist name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                createPlaylist(name.trim());
            }
        });
    }

    public void addToPlaylist(int playlistId, int songId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    musicService.addSongToPlaylist(playlistId, songId);
                    return null;
                } catch (RemoteException e) {
                    throw new Exception("Failed to add song to playlist: " + e.getMessage(), e);
                }
            }
        };
        
        task.setOnSucceeded(e -> {
            showSuccessAlert("Song added to playlist");
        });
        
        task.setOnFailed(e -> {
            if (task.getException() != null) {
                showErrorAlert("Failed to add song: " + task.getException().getMessage());
            } else {
                showErrorAlert("Failed to add song: Unknown error.");
            }
        });
        new Thread(task).start();
    }

    public void createPlaylist(String name) {
        Task<Playlist> task = new Task<>() {
            @Override
            protected Playlist call() throws Exception {
                try {
                    return musicService.createPlaylist(userId, name);
                } catch (RemoteException e) {
                    throw new Exception("Failed to create playlist: " + e.getMessage(), e);
                }
            }
        };
        
        task.setOnSucceeded(e -> {
            Playlist createdPlaylist = task.getValue();
            if (createdPlaylist != null) {
                loadPlaylists();
                showSuccessAlert("Playlist '" + createdPlaylist.getName() + "' created");
            } else {
                showErrorAlert("Failed to create playlist (server returned null).");
            }
        });
        task.setOnFailed(e -> {
            if (task.getException() != null) {
                showErrorAlert("Error creating playlist: " + task.getException().getMessage());
            } else {
                showErrorAlert("Error creating playlist: Unknown error.");
            }
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