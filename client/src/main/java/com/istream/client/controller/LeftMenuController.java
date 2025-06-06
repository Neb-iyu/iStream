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
    private Consumer<Void> onNewPlaylistClick;

    public void initServices(RMIClient rmiClient) {
        this.rmiClient = rmiClient;
        System.out.println("Initializing LeftMenuController services");
        loadPlaylists();
        checkAdminStatus();
    }

    private void checkAdminStatus() {
        System.out.println("Checking admin status for user: " + rmiClient.getUserId());
        
        Task<Boolean> adminTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                boolean isAdmin = rmiClient.isAdmin(rmiClient.getUserId());
                System.out.println("Admin status check result: " + isAdmin);
                return isAdmin;
            }
        };

        adminTask.setOnSucceeded(event -> {
            boolean isAdmin = adminTask.getValue();
            Platform.runLater(() -> {
                uploadButton.setVisible(isAdmin);
                System.out.println("Upload button visibility set to: " + isAdmin);
            });
        });

        adminTask.setOnFailed(event -> {
            String errorMsg = "Failed to check admin status: " + adminTask.getException().getMessage();
            System.err.println(errorMsg);
            adminTask.getException().printStackTrace();
            Platform.runLater(() -> UiComponent.showError("Error", errorMsg));
        });

        new Thread(adminTask).start();
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

    public void setOnNewPlaylistClick(Consumer<Void> callback) {
        this.onNewPlaylistClick = callback;
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
    private void handleNewPlaylistClick() {
        if (onNewPlaylistClick != null) {
            onNewPlaylistClick.accept(null);
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
        System.out.println("Loading playlists for user: " + rmiClient.getUserId());
        
        Task<List<Playlist>> playlistTask = new Task<List<Playlist>>() {
            @Override
            protected List<Playlist> call() throws Exception {
                List<Playlist> playlists = rmiClient.getUserPlaylists();
                System.out.println("Successfully loaded " + playlists.size() + " playlists");
                return playlists;
            }
        };

        playlistTask.setOnSucceeded(event -> {
            List<Playlist> playlists = playlistTask.getValue();
            Platform.runLater(() -> {
                playlistsBox.getChildren().clear(); // Clear existing playlists
                for (Playlist playlist : playlists) {
                    Button playlistButton = new Button(playlist.getName());
                    playlistButton.getStyleClass().add("playlist-button");
                    playlistButton.setOnAction(event2 -> handlePlaylistClick(playlist));
                    playlistsBox.getChildren().add(playlistButton);
                    System.out.println("Added playlist button: " + playlist.getName());
                }
            });
        });

        playlistTask.setOnFailed(event -> {
            String errorMsg = "Failed to load playlists: " + playlistTask.getException().getMessage();
            System.err.println(errorMsg);
            playlistTask.getException().printStackTrace();
            Platform.runLater(() -> UiComponent.showError("Error", errorMsg));
        });

        new Thread(playlistTask).start();
    }

    private void handlePlaylistClick(Playlist playlist) {
        System.out.println("Handling click for playlist: " + playlist.getName());
        try {
            Parent playlistPage = PlaylistPageController.createView();
            PlaylistPageController controller = (PlaylistPageController) playlistPage.getUserData();
            controller.setRMIClient(rmiClient);
            controller.loadPlaylist(playlist);
            
            if (onPlaylistClick != null) {
                onPlaylistClick.accept(playlistPage);
                System.out.println("Successfully loaded playlist page: " + playlist.getName());
            }
        } catch (Exception ex) {
            String errorMsg = "Failed to load playlist: " + ex.getMessage();
            System.err.println(errorMsg);
            ex.printStackTrace();
            Platform.runLater(() -> UiComponent.showError("Error", errorMsg));
        }
    }

    @FXML
    private void createPlaylist() {
        String name = UiComponent.showInputDialog("Create Playlist", "Enter playlist name:");
        if (name != null && !name.trim().isEmpty()) {
            System.out.println("Creating new playlist: " + name);
            
            Task<Playlist> createTask = new Task<Playlist>() {
                @Override
                protected Playlist call() throws Exception {
                    Playlist playlist = rmiClient.createPlaylist(name);
                    System.out.println("Successfully created playlist: " + name);
                    return playlist;
                }
            };

            createTask.setOnSucceeded(event -> {
                Playlist playlist = createTask.getValue();
                Platform.runLater(() -> {
                    Button playlistButton = new Button(playlist.getName());
                    playlistButton.getStyleClass().add("playlist-button");
                    playlistButton.setOnAction(event2 -> handlePlaylistClick(playlist));
                    playlistsBox.getChildren().add(playlistButton);
                    System.out.println("Added new playlist button: " + playlist.getName());
                });
            });

            createTask.setOnFailed(event -> {
                String errorMsg = "Failed to create playlist: " + createTask.getException().getMessage();
                System.err.println(errorMsg);
                createTask.getException().printStackTrace();
                Platform.runLater(() -> UiComponent.showError("Error", errorMsg));
            });

            new Thread(createTask).start();
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