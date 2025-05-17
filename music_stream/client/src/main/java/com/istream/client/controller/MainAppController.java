package com.istream.client.controller;

import com.istream.client.util.SessionHolder;
import com.istream.rmi.MusicService;
import com.istream.client.service.RMIClient;
import com.istream.client.view.HomeView;
import com.istream.client.view.LikedView;
import com.istream.client.view.ArtistsView;
import com.istream.client.view.AlbumView;
import com.istream.client.view.ArtistView;
import com.istream.client.util.UiComponent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class MainAppController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox contentArea;

    // These fields will be automatically injected if the fx:id in FXML matches the field name
    // AND the included FXML has a controller that is an instance of PlaylistController.
    // This requires LeftMenuPane.fxml to have fx:controller="com.istream.client.controller.PlaylistController"
    @FXML private Parent leftMenuPane; // The root node of LeftMenuPane.fxml
    @FXML private LeftMenuController leftMenuPaneController; // The controller of LeftMenuPane.fxml

    private MusicService musicService;
    private RMIClient rmiClient;
    private SessionHolder sessionHolder;

    // No-arg constructor for FXML loader
    public MainAppController() {}

    // Called after FXML fields are injected
    @FXML
    public void initialize() {
        // Services will be set by ClientMain calling initializeServices later
        // Initial view loading will also happen in initializeServices
    }

    public void initializeServices(MusicService musicService, RMIClient rmiClient, SessionHolder sessionHolder) {
        this.musicService = musicService;
        this.rmiClient = rmiClient;
        this.sessionHolder = sessionHolder;

        if (leftMenuPaneController != null) {
            int userId = sessionHolder.getCurrentUserId();
            leftMenuPaneController.initServices(musicService, userId);
            
            // Set up navigation callbacks
            leftMenuPaneController.setOnHomeClick(this::loadHomeView);
            leftMenuPaneController.setOnLikedClick(this::loadLikedView);
            leftMenuPaneController.setOnArtistsClick(this::loadArtistsView);
            
            // Load home view by default
            loadHomeView();
        } else {
            System.err.println("MainAppController: leftMenuPaneController is null. Check FXML setup for LeftMenuPane.fxml.");
        }
    }

    private void loadHomeView() {
        try {
            int userId = sessionHolder.getCurrentUserId();
            HomeView homeView = new HomeView(musicService, userId);
            contentArea.getChildren().setAll(homeView);
        } catch (IOException e) {
            e.printStackTrace();
            UiComponent.showError("Could not load home view", e.getMessage());
        }
    }

    private void loadLikedView() {
        try {
            int userId = sessionHolder.getCurrentUserId();
            LikedView likedView = new LikedView(musicService, userId, this);
            contentArea.getChildren().setAll(likedView);
        } catch (IOException e) {
            e.printStackTrace();
            UiComponent.showError("Could not load liked view", e.getMessage());
        }
    }

    private void loadArtistsView() {
        try {
            int userId = sessionHolder.getCurrentUserId();
            ArtistsView artistsView = new ArtistsView(musicService, userId, this);
            contentArea.getChildren().setAll(artistsView);
        } catch (IOException e) {
            e.printStackTrace();
            UiComponent.showError("Could not load artists view", e.getMessage());
        }
    }

    private void loadAlbumView() {
        try {
            int userId = sessionHolder.getCurrentUserId();
            AlbumView albumView = new AlbumView(musicService, userId, this);
            contentArea.getChildren().setAll(albumView);
        } catch (IOException e) {
            e.printStackTrace();
            UiComponent.showError("Could not load album view", e.getMessage());
        }
    }
    

    public void setContent(Parent newContent) {
        contentArea.getChildren().setAll(newContent);
    }

 
} 