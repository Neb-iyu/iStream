package com.istream.client.controller;

import com.istream.rmi.MusicService;
import com.istream.model.Playlist;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LeftMenuController {
    @FXML private AnchorPane root;
    @FXML private Button homeButton;
    @FXML private Button likedButton;
    @FXML private Button artistsButton;
    @FXML private Button newPlaylistButton;
    @FXML private ListView<Playlist> playlistListView;

    private Runnable onHomeClick;
    private Runnable onLikedClick;
    private Runnable onArtistsClick;
    private Runnable onNewPlaylistClick;
    private PlaylistController playlistController;
    private MusicService musicService;
    private int userId;

    @FXML
    public void initialize() {
        setupEventHandlers();
        playlistListView.setItems(FXCollections.observableArrayList());
    }

    private void setupEventHandlers() {
        homeButton.setOnAction(e -> handleHomeClick());
        likedButton.setOnAction(e -> handleLikedClick());
        artistsButton.setOnAction(e -> handleArtistsClick());
        newPlaylistButton.setOnAction(e -> handleNewPlaylistClick());
    }

    private void handleHomeClick() {
        if (onHomeClick != null) {
            onHomeClick.run();
        }
    }

    private void handleLikedClick() {
        if (onLikedClick != null) {
            onLikedClick.run();
        }
    }

    private void handleArtistsClick() {
        if (onArtistsClick != null) {
            onArtistsClick.run();
        }
    }

    private void handleNewPlaylistClick() {
        if (playlistController != null) {
            playlistController.handleNewPlaylistClick();
        }
    }

    public void initServices(MusicService musicService, int userId) {
        this.musicService = musicService;
        this.userId = userId;
        
        // Initialize PlaylistController
        playlistController = new PlaylistController();
        playlistController.initServices(musicService, userId);
        
        // Set up playlist list view
        if (playlistListView != null) {
            playlistListView.setItems(playlistController.getPlaylists());
        }
    }

    public void setOnHomeClick(Runnable callback) {
        this.onHomeClick = callback;
    }

    public void setOnLikedClick(Runnable callback) {
        this.onLikedClick = callback;
    }

    public void setOnArtistsClick(Runnable callback) {
        this.onArtistsClick = callback;
    }

    public void setOnNewPlaylistClick(Runnable callback) {
        this.onNewPlaylistClick = callback;
    }

    public static Parent createView() {
        try {
            FXMLLoader loader = new FXMLLoader(LeftMenuController.class.getResource("/com/istream/fxml/alwaysOnDisplay/LeftMenuPane.fxml"));
            loader.setControllerFactory(param -> new LeftMenuController());
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load LeftMenuPane.fxml", e);
        }
    }
} 