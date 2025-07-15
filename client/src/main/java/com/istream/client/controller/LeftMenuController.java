package com.istream.client.controller;

import java.util.List;
import java.util.function.Consumer;

import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import com.istream.client.util.ThreadManager;
import com.istream.model.Playlist;
import com.istream.client.view.PlaylistView;

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
    private MainAppController mainAppController;

    public void initServices(RMIClient rmiClient, MainAppController mainAppController) {
        this.rmiClient = rmiClient;
        this.mainAppController = mainAppController;
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
            ThreadManager.runOnFxThread(() -> {
                uploadButton.setVisible(isAdmin);
                System.out.println("Upload button visibility set to: " + isAdmin);
            });
        });

        adminTask.setOnFailed(event -> {
            String errorMsg = "Failed to check admin status: " + adminTask.getException().getMessage();
            System.err.println(errorMsg);
            adminTask.getException().printStackTrace();
            ThreadManager.runOnFxThread(() -> UiComponent.showError("Error", errorMsg));
        });

        ThreadManager.submitTask(adminTask);
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
            ThreadManager.runOnFxThread(() -> {
                if (playlistsBox == null) {
                    System.err.println("Warning: playlistsBox is null");
                    return;
                }
                
                if (playlists == null || playlists.isEmpty()) {
                    UiComponent.createEmptyPlaylistMessage(playlistsBox);
                    return;
                }

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
            ThreadManager.runOnFxThread(() -> UiComponent.showError("Error", errorMsg));
        });

        ThreadManager.submitTask(playlistTask);
    }

    private void handlePlaylistClick(Playlist playlist) {
        System.out.println("Handling click for playlist: " + playlist.getName());
        try {
            PlaylistView playlistView = new PlaylistView(rmiClient, mainAppController, playlist);
            if (onPlaylistClick != null) {
                onPlaylistClick.accept(playlistView);
                System.out.println("Successfully loaded playlist view: " + playlist.getName());
            }
        } catch (Exception ex) {
            String errorMsg = "Failed to load playlist: " + ex.getMessage();
            System.err.println(errorMsg);
            ex.printStackTrace();
            ThreadManager.runOnFxThread(() -> UiComponent.showError("Error", errorMsg));
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
                ThreadManager.runOnFxThread(() -> {
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
                ThreadManager.runOnFxThread(() -> UiComponent.showError("Error", errorMsg));
            });

            ThreadManager.submitTask(createTask);
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