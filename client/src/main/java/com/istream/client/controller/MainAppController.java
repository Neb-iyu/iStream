package com.istream.client.controller;

import java.io.IOException;

import com.istream.client.service.AudioService;
import com.istream.client.service.RMIClient;
import com.istream.client.service.SearchService;
import com.istream.client.util.SessionHolder;
import com.istream.client.util.UiComponent;
import com.istream.client.view.AlbumView;
import com.istream.client.view.ArtistView;
import com.istream.client.view.ArtistsView;
import com.istream.client.view.HomeView;
import com.istream.client.view.LikedView;
import com.istream.client.view.PlaylistsView;
import com.istream.client.view.ProfileView;
import com.istream.model.Song;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;

public class MainAppController {
    @FXML private BorderPane rootPane;
    @FXML private VBox contentArea;
    @FXML private Parent leftMenuPane;
    @FXML private LeftMenuController leftMenuPaneController;
    @FXML private Parent topMenuPane;
    @FXML private TopMenuController topMenuController;
    @FXML private Parent playerBar;
    @FXML private PlayerBarController playerBarController;

    private RMIClient rmiClient;
    private SessionHolder sessionHolder;
    private AudioService audioService;
    private SearchService searchService;

    public MainAppController() {
        this.audioService = new AudioService();
    }

    @FXML
    public void initialize() {
        try {
            // Load CSS
            Scene scene = rootPane.getScene();
            if (scene != null) {
                scene.getStylesheets().add(getClass().getResource("/com/istream/css/common.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/com/istream/css/components.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/com/istream/css/pages.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/com/istream/css/animations.css").toExternalForm());
            }

            // Initialize the left menu
            if (leftMenuPaneController == null) {
                System.err.println("Warning: leftMenuPaneController is null. Will be initialized in initializeServices.");
            }

            // Initialize the top menu
            if (topMenuController == null) {
                topMenuController = new TopMenuController();
            }

            // Initialize the player bar
            if (playerBarController != null) {
                playerBarController.setAudioService(audioService);
            }
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void initializeServices(RMIClient rmiClient, SessionHolder sessionHolder) {
        this.rmiClient = rmiClient;
        this.sessionHolder = sessionHolder;

        try {
            // Initialize the left menu if not already done
            if (leftMenuPaneController == null) {
                FXMLLoader leftMenuLoader = new FXMLLoader(getClass().getResource("/com/istream/fxml/alwaysOnDisplay/LeftMenuPane.fxml"));
                Parent leftMenuView = leftMenuLoader.load();
                leftMenuPaneController = leftMenuLoader.getController();
                
                if (leftMenuPane instanceof Pane) {
                    ((Pane)leftMenuPane).getChildren().add(leftMenuView);
                }
            }

            // Initialize left menu services and callbacks
            if (leftMenuPaneController != null) {
                leftMenuPaneController.initServices(rmiClient);
                leftMenuPaneController.setOnHomeClick(v -> loadHomeView());
                leftMenuPaneController.setOnLikedClick(v -> loadLikedView());
                leftMenuPaneController.setOnArtistsClick(v -> loadArtistsView());
                leftMenuPaneController.setOnPlaylistClick(content -> contentArea.getChildren().setAll(content));
                leftMenuPaneController.setOnUploadClick(content -> contentArea.getChildren().setAll(content));
                leftMenuPaneController.setOnNewPlaylistClick(v -> loadPlaylistsView());
            } else {
                System.err.println("Error: Failed to initialize leftMenuPaneController");
            }

            // Initialize top menu services
            if (topMenuController != null) {
                topMenuController.initServices(rmiClient);
                topMenuController.setOnSearchByName(this::handleSearchByName);
                topMenuController.setOnProfileClick(this::handleProfileClick);
                topMenuController.setOnResultClick(this::handleSearchResult);
                searchService = new SearchService(rmiClient, topMenuController);
            }

            // Initialize player bar
            if (playerBarController != null) {
                playerBarController.setRMIClient(rmiClient);
            }

            // Load home view by default
            loadHomeView();
        } catch (Exception e) {
            System.err.println("Error during service initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showPlayerBar() {
        if (rootPane.getRight() == null) {
            rootPane.setRight(playerBar);
            if (playerBarController != null) {
                playerBarController.setAudioService(audioService);
                playerBarController.setRMIClient(rmiClient);
                playerBarController.setSessionHolder(sessionHolder);
            }
        }
    }

    private void hidePlayerBar() {
        if (audioService != null && !audioService.isPlaying() && audioService.getSongQueue().isEmpty()) {
            rootPane.setRight(null);
        }
    }

    public void playSong(Song song) {
        try {
            byte[] audioData = rmiClient.streamSong(song.getId());
            audioService.playSong(song, audioData);
            if (playerBarController != null) {
                playerBarController.updateSongInfo(song);
            }
            showPlayerBar();
        } catch (Exception e) {
            UiComponent.showError("Error", "Failed to play song: " + e.getMessage());
        }
    }

    public void addToQueue(Song song) {
        try {
            byte[] audioData = rmiClient.streamSong(song.getId());
            audioService.addToQueueAsync(song, audioData).thenRun(() -> {
                if (playerBarController != null) {
                    playerBarController.updateQueueDisplay();
                }
                showPlayerBar();
            }).exceptionally(ex -> {
                UiComponent.showError("Error", "Failed to add song to queue: " + ex.getMessage());
                return null;
            });

            showPlayerBar();
        } catch (Exception e) {
            UiComponent.showError("Error", "Failed to add song to queue: " + e.getMessage());
        }
    }

    private void loadHomeView() {
        try {
            HomeView homeView = new HomeView(rmiClient, this);
            contentArea.getChildren().setAll(homeView);
        } catch (IOException e) {
            e.printStackTrace();
            UiComponent.showError("Could not load home view", e.getMessage());
        }
    }

    private void loadLikedView() {
        try {
            LikedView likedView = new LikedView(rmiClient, this);
            contentArea.getChildren().setAll(likedView);
        } catch (IOException e) {
            e.printStackTrace();
            UiComponent.showError("Could not load liked view", e.getMessage());
        }
    }

    private void loadArtistsView() {
        try {
            ArtistsView artistsView = new ArtistsView(rmiClient, this);
            contentArea.getChildren().setAll(artistsView);
        } catch (IOException e) {
            e.printStackTrace();
            UiComponent.showError("Could not load artists view", e.getMessage());
        }
    }

    private void loadPlaylistsView() {
        try {
            PlaylistsView playlistsView = new PlaylistsView(rmiClient, this);
            contentArea.getChildren().setAll(playlistsView);
        } catch (Exception e) {
            e.printStackTrace();
            UiComponent.showError("Could not load playlists view", e.getMessage());
        }
    }

    public void loadAlbumView(int albumId) {
        try {
            AlbumView albumView = new AlbumView(rmiClient, albumId, this);
            contentArea.getChildren().setAll(albumView);
        } catch (IOException e) {
            e.printStackTrace();
            UiComponent.showError("Could not load album view", e.getMessage());
        }
    }

    public void loadArtistView(int artistId) {
        try {
            ArtistView artistView = new ArtistView(rmiClient, artistId, this);
            contentArea.getChildren().setAll(artistView);
        } catch (IOException e) {
            e.printStackTrace();
            UiComponent.showError("Could not load artist view", e.getMessage());
        }
    }

    public void setContent(Parent newContent) {
        contentArea.getChildren().setAll(newContent);
    }

    private void handleSearchByName() {
        // The actual search is handled in SearchService
        // This method is just a callback for the search field
    }

    private void handleProfileClick() {
        try {
            ProfileView profileView = new ProfileView(rmiClient, this);
            contentArea.getChildren().setAll(profileView);
        } catch (IOException e) {
            e.printStackTrace();
            UiComponent.showError("Could not load profile view", e.getMessage());
        }
    }

    private void handleSearchResult(TopMenuController.SearchResult result) {
        switch (result.getType()) {
            case "song":
                try {
                    Song song = rmiClient.getSongById(result.getId());
                    if (song != null) {
                        playSong(song);
                    }
                } catch (Exception e) {
                    UiComponent.showError("Error", "Failed to play song: " + e.getMessage());
                }
                break;
            case "artist":
                loadArtistView(result.getId());
                break;
            case "album":
                loadAlbumView(result.getId());
                break;
        }
    }

    public AudioService getAudioService() {
        return audioService;
    }

    public PlayerBarController getPlayerBarController() {
        return playerBarController;
    }
}