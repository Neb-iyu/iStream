package com.istream.client.controller;

import java.util.List;

import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import com.istream.client.util.AnimationUtil;
import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.Song;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class HomeViewController {
    @FXML private VBox listenAgainContainer;
    @FXML private VBox artistsContainer;
    @FXML private VBox albumsContainer;
    @FXML private HBox listenAgainBox;
    @FXML private HBox artistsBox;
    @FXML private HBox albumsBox;

    private final RMIClient rmiClient;
    private final MainAppController mainAppController;

    public HomeViewController(RMIClient rmiClient, MainAppController mainAppController) {
        this.rmiClient = rmiClient;
        this.mainAppController = mainAppController;
    }

    @FXML
    public void initialize() {
        // Animate containers with a slight delay between each
        AnimationUtil.slideIn(listenAgainContainer, 500);
        AnimationUtil.slideIn(artistsContainer, 700);
        AnimationUtil.slideIn(albumsContainer, 900);
        
        // Add hover effects to content rows
        listenAgainBox.getStyleClass().add("hover-scale");
        artistsBox.getStyleClass().add("hover-scale");
        albumsBox.getStyleClass().add("hover-scale");

        // Load data
        loadListenAgainSongs();
        loadArtists();
        loadAlbums();
    }

    private void loadListenAgainSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return rmiClient.getHistorySongs();
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            Platform.runLater(() -> {
                if (listenAgainBox == null) {
                    System.err.println("Warning: listenAgainBox is null");
                    return;
                }
                listenAgainBox.getChildren().clear();
                if (songs == null || songs.isEmpty()) {
                    // Show empty state message
                    Label emptyLabel = new Label("No recently played songs");
                    emptyLabel.getStyleClass().add("empty-state-label");
                    listenAgainBox.getChildren().add(emptyLabel);
                } else {
                    UiComponent.createSongRow(songs, listenAgainBox, rmiClient, mainAppController);
                }
            });
        });

        task.setOnFailed(e -> {
            String errorMsg = "Failed to load recently played songs: " + task.getException().getMessage();
            System.err.println(errorMsg);
            task.getException().printStackTrace();
            Platform.runLater(() -> {
                if (listenAgainBox != null) {
                    listenAgainBox.getChildren().clear();
                    Label errorLabel = new Label("Failed to load recently played songs");
                    errorLabel.getStyleClass().add("error-state-label");
                    listenAgainBox.getChildren().add(errorLabel);
                }
                UiComponent.showError("Error", errorMsg);
            });
        });

        new Thread(task).start();
    }

    private void loadArtists() {
        Task<List<Artist>> task = new Task<>() {
            @Override
            protected List<Artist> call() throws Exception {
                return rmiClient.getAllArtists();
            }
        };

        task.setOnSucceeded(e -> {
            List<Artist> artists = task.getValue();
            Platform.runLater(() -> {
                if (artistsBox == null) {
                    System.err.println("Warning: artistsBox is null");
                    return;
                }
                UiComponent.createArtistRow(artists, artistsBox, rmiClient, mainAppController);
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load artists: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }

    private void loadAlbums() {
        Task<List<Album>> task = new Task<>() {
            @Override
            protected List<Album> call() throws Exception {
                return rmiClient.getAllAlbums();
            }
        };

        task.setOnSucceeded(e -> {
            List<Album> albums = task.getValue();
            Platform.runLater(() -> {
                if (albumsBox == null) {
                    System.err.println("Warning: albumsBox is null");
                    return;
                }
                UiComponent.createAlbumRow(albums, albumsBox, rmiClient, mainAppController);
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load albums: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }

    /**
     * Refreshes the content with animations
     */
    public void refreshContent() {
        // Fade out current content
        AnimationUtil.fadeOut(listenAgainContainer, 300);
        AnimationUtil.fadeOut(artistsContainer, 300);
        AnimationUtil.fadeOut(albumsContainer, 300);
        
        // After fade out, update content and fade in
        Platform.runLater(() -> {
            // Clear existing content
            listenAgainBox.getChildren().clear();
            artistsBox.getChildren().clear();
            albumsBox.getChildren().clear();
            
            // Reload data
            loadListenAgainSongs();
            loadArtists();
            loadAlbums();
            
            // Fade in with delay
            AnimationUtil.fadeIn(listenAgainContainer, 300);
            AnimationUtil.fadeIn(artistsContainer, 500);
            AnimationUtil.fadeIn(albumsContainer, 700);
        });
    }
    
    /**
     * Shows a loading animation
     */
    public void showLoading() {
        listenAgainContainer.getStyleClass().add("loading-spinner");
        artistsContainer.getStyleClass().add("loading-spinner");
        albumsContainer.getStyleClass().add("loading-spinner");
    }
    
    /**
     * Hides the loading animation
     */
    public void hideLoading() {
        listenAgainContainer.getStyleClass().remove("loading-spinner");
        artistsContainer.getStyleClass().remove("loading-spinner");
        albumsContainer.getStyleClass().remove("loading-spinner");
    }
}
