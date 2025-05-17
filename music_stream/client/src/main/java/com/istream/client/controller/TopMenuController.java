package com.istream.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class TopMenuController {
    @FXML private AnchorPane root;
    @FXML private Pane searchPane;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ImageView profileImage;
    @FXML private Pane leftPane;

    private Runnable onSearch;
    private Runnable onProfileClick;

    @FXML
    public void initialize() {
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> handleSearch());
        profileImage.setOnMouseClicked(e -> handleProfileClick());
    }

    private void handleSearch() {
        String searchQuery = searchField.getText();
        if (onSearch != null) {
            onSearch.run();
        }
    }

    private void handleProfileClick() {
        if (onProfileClick != null) {
            onProfileClick.run();
        }
    }

    public void setOnSearch(Runnable callback) {
        this.onSearch = callback;
    }

    public void setOnProfileClick(Runnable callback) {
        this.onProfileClick = callback;
    }

    public static Parent createView() {
        try {
            FXMLLoader loader = new FXMLLoader(TopMenuController.class.getResource("/com/istream/fxml/alwaysOnDisplay/TopMenuPane.fxml"));
            loader.setControllerFactory(param -> new TopMenuController());
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load TopMenuPane.fxml", e);
        }
    }
} 