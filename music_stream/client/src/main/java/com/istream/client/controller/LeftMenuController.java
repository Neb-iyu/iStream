package com.istream.client.controller;

import java.util.List;
import java.util.function.Consumer;

import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import com.istream.model.Playlist;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class LeftMenuController {
    @FXML private VBox playlistsBox;
    @FXML private Button createPlaylistButton;
    @FXML private Button uploadButton;

    private RMIClient rmiClient;
    private Consumer<Void> onHomeClick;
    private Consumer<Void> onLikedClick;
    private Consumer<Void> onArtistsClick;
    private Consumer<Parent> onPlaylistClick;
    private Consumer<Parent> onUploadClick;

    public void initServices(RMIClient rmiClient) {
        this.rmiClient = rmiClient;
        loadPlaylists();
        checkAdminStatus();
    }

    private void checkAdminStatus() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return rmiClient.isAdmin(rmiClient.getUserId());
            }
        };

        task.setOnSucceeded(e -> {
            boolean isAdmin = task.getValue();
            uploadButton.setVisible(isAdmin);
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to check admin status: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }

    public void setOnHomeClick(Consumer<Void> callback) {
        this.onHomeClick = callback;
    }

    public void setOnLikedClick(Consumer<Void> callback) {
        this.onLikedClick = callback;
    }

    public void setOnArtistsClick(Consumer<Void> callback) {
        this.onArtistsClick = callback;
    }

    public void setOnPlaylistClick(Consumer<Parent> callback) {
        this.onPlaylistClick = callback;
    }

    public void setOnUploadClick(Consumer<Parent> callback) {
        this.onUploadClick = callback;
    }

    @FXML
    private void handleHomeClick() {
        if (onHomeClick != null) {
            onHomeClick.accept(null);
        }
    }

    @FXML
    private void handleLikedClick() {
        if (onLikedClick != null) {
            onLikedClick.accept(null);
        }
    }

    @FXML
    private void handleArtistsClick() {
        if (onArtistsClick != null) {
            onArtistsClick.accept(null);
        }
    }

    @FXML
    private void handleUploadClick() {
        if (onUploadClick != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/UploadView.fxml"));
                Parent uploadView = loader.load();
                UploadController uploadController = loader.getController();
                uploadController.initServices(rmiClient);
                onUploadClick.accept(uploadView);
            } catch (Exception e) {
                UiComponent.showError("Error", "Failed to load upload view: " + e.getMessage());
            }
        }
    }

    private void loadPlaylists() {
        Task<List<Playlist>> task = new Task<>() {
            @Override
            protected List<Playlist> call() throws Exception {
                return rmiClient.getUserPlaylists();
            }
        };

        task.setOnSucceeded(e -> {
            List<Playlist> playlists = task.getValue();
            for (Playlist playlist : playlists) {
                Button playlistButton = new Button(playlist.getName());
                playlistButton.getStyleClass().add("playlist-button");
                playlistButton.setOnAction(event -> handlePlaylistClick(playlist));
                playlistsBox.getChildren().add(playlistButton);
            }
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load playlists: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }

    private void handlePlaylistClick(Playlist playlist) {
        try {
            Parent playlistPage = PlaylistPageController.createView();
            PlaylistPageController controller = (PlaylistPageController) playlistPage.getUserData();
            controller.setRMIClient(rmiClient);
            controller.loadPlaylist(playlist);
            
            // Update the main content area
            if (onPlaylistClick != null) {
                onPlaylistClick.accept(playlistPage);
            }
        } catch (Exception ex) {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load playlist: " + ex.getMessage())
            );
        }
    }

    @FXML
    private void createPlaylist() {
        String name = UiComponent.showInputDialog("Create Playlist", "Enter playlist name:");
        if (name != null && !name.trim().isEmpty()) {
            Task<Playlist> task = new Task<>() {
                @Override
                protected Playlist call() throws Exception {
                    return rmiClient.createPlaylist(name);
                }
            };

            task.setOnSucceeded(e -> {
                Playlist playlist = task.getValue();
                Button playlistButton = new Button(playlist.getName());
                playlistButton.getStyleClass().add("playlist-button");
                playlistsBox.getChildren().add(playlistButton);
            });

            task.setOnFailed(e -> {
                Platform.runLater(() -> 
                    UiComponent.showError("Error", "Failed to create playlist: " + task.getException().getMessage())
                );
            });

            new Thread(task).start();
        }
    }

    public static Parent createView() {
        try {
            FXMLLoader loader = new FXMLLoader(LeftMenuController.class.getResource("/com/istream/fxml/alwaysOnDisplay/LeftMenuPane.fxml"));
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load LeftMenuPane.fxml", e);
        }
    }
}