package com.istream.client.controller;

import com.istream.client.service.RMIClient;
import com.istream.client.util.ThreadManager;
import com.istream.client.util.UiComponent;
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

    private RMIClient rmiClient;
    private int userId;
    private ObservableList<Playlist> playlists = FXCollections.observableArrayList();

    public PlaylistController() {}

    public void initServices(RMIClient rmiClient, int userId) {
        this.rmiClient = rmiClient;
        this.userId = userId;
        if (playlistListView != null && this.rmiClient != null) {
            loadPlaylists();
        }
    }

    @FXML
    public void initialize() {
        if (playlistListView != null) {
            playlistListView.setItems(playlists);
        }
        if (rmiClient != null) {
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
                    return rmiClient.getUserPlaylists();
                } catch (RemoteException e) {
                    throw new Exception("Failed to load playlists: " + e.getMessage(), e);
                }
            }
        };
        
        task.setOnSucceeded(e -> {
            ThreadManager.runOnFxThread(() -> playlists.setAll(task.getValue()));
        });
        
        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> 
                UiComponent.showError("Error", "Failed to load playlists: " + task.getException().getMessage())
            );
        });
        
        ThreadManager.submitTask(task);
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
                    rmiClient.addSongToPlaylist(playlistId, songId);
                    return null;
                } catch (RemoteException e) {
                    throw new Exception("Failed to add song to playlist: " + e.getMessage(), e);
                }
            }
        };
        
        task.setOnSucceeded(e -> {
            ThreadManager.runOnFxThread(() -> 
                UiComponent.showSuccess("Success", "Song added to playlist")
            );
        });
        
        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> 
                UiComponent.showError("Error", "Failed to add song: " + task.getException().getMessage())
            );
        });
        
        ThreadManager.submitTask(task);
    }

    public void createPlaylist(String name) {
        Task<Playlist> task = new Task<>() {
            @Override
            protected Playlist call() throws Exception {
                try {
                    return rmiClient.createPlaylist(name);
                } catch (RemoteException e) {
                    throw new Exception("Failed to create playlist: " + e.getMessage(), e);
                }
            }
        };
        
        task.setOnSucceeded(e -> {
            Playlist createdPlaylist = task.getValue();
            if (createdPlaylist != null) {
                loadPlaylists();
                ThreadManager.runOnFxThread(() -> 
                    UiComponent.showSuccess("Success", "Playlist '" + createdPlaylist.getName() + "' created")
                );
            } else {
                ThreadManager.runOnFxThread(() -> 
                    UiComponent.showError("Error", "Failed to create playlist (server returned null)")
                );
            }
        });
        
        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> 
                UiComponent.showError("Error", "Failed to create playlist: " + task.getException().getMessage())
            );
        });
        
        ThreadManager.submitTask(task);
    }
}