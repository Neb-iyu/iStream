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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

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
        // Load CSS
        Scene scene = rootPane.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/com/istream/styles/main.css").toExternalForm());
        }

        // Initialize the left menu
        leftMenuPaneController = new LeftMenuController();
        leftMenuPaneController.initServices(rmiClient);
        leftMenuPaneController.setOnHomeClick(v -> loadHomeView());
        leftMenuPaneController.setOnLikedClick(v -> loadLikedView());
        leftMenuPaneController.setOnArtistsClick(v -> loadArtistsView());
        leftMenuPaneController.setOnPlaylistClick(content -> contentArea.getChildren().setAll(content));
        leftMenuPaneController.setOnUploadClick(content -> contentArea.getChildren().setAll(content));
        ((VBox)leftMenuPane).getChildren().add(leftMenuPaneController.createView());

        // Initialize the top menu
        topMenuController = new TopMenuController();
        topMenuController.initServices(rmiClient);
        topMenuController.setOnSearchByName(this::handleSearchByName);
        topMenuController.setOnProfileClick(this::handleProfileClick);
        topMenuController.setOnResultClick(this::handleSearchResult);
        ((VBox)topMenuPane).getChildren().add(topMenuController.createView());

        // Initialize search service
        searchService = new SearchService(rmiClient, topMenuController);

        // Initialize the player bar
        playerBarController.setAudioService(audioService);
        playerBarController.setRMIClient(rmiClient);

        // Load the home view by default
        loadHomeView();
    }

    public void initializeServices(RMIClient rmiClient, SessionHolder sessionHolder) {
        this.rmiClient = rmiClient;
        this.sessionHolder = sessionHolder;

        if (leftMenuPaneController != null) {
            leftMenuPaneController.initServices(rmiClient);
            
            // Set up navigation callbacks
            leftMenuPaneController.setOnHomeClick(v -> loadHomeView());
            leftMenuPaneController.setOnLikedClick(v -> loadLikedView());
            leftMenuPaneController.setOnArtistsClick(v -> loadArtistsView());
            
            // Load home view by default
            loadHomeView();
        } else {
            System.err.println("MainAppController: leftMenuPaneController is null. Check FXML setup for LeftMenuPane.fxml.");
        }

        // Update player bar with RMI client
        if (playerBarController != null) {
            playerBarController.setRMIClient(rmiClient);
        }

        // Initialize search service
        if (topMenuController != null) {
            searchService = new SearchService(rmiClient, topMenuController);
        }
    }

    public void playSong(Song song) {
        try {
            byte[] audioData = rmiClient.streamSong(song.getId());
            audioService.playSong(song, audioData);
            playerBarController.updateSongInfo(song);
            showPlayerBar();
        } catch (Exception e) {
            UiComponent.showError("Error", "Failed to play song: " + e.getMessage());
        }
    }

    private void showPlayerBar() {
        if (rootPane.getRight() == null) {
            rootPane.setRight(playerBar);
        }
    }

    private void hidePlayerBar() {
        rootPane.setRight(null);
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
}