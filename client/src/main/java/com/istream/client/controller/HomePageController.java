package com.istream.client.controller;

import com.istream.client.util.AnimationUtil;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomePageController {
    @FXML
    private VBox listenAgainContainer;
    
    @FXML
    private VBox artistsContainer;
    
    @FXML
    private VBox albumsContainer;
    
    @FXML
    private HBox listenAgainBox;
    
    @FXML
    private HBox artistsBox;
    
    @FXML
    private HBox albumsBox;
    
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
        javafx.application.Platform.runLater(() -> {
            // Update content here
            
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