package com.istream.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import com.istream.model.User;

public class TopMenuController {
    @FXML private AnchorPane root;
    @FXML private Pane searchPane;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ImageView profileImage;
    @FXML private Pane leftPane;
    @FXML private VBox searchResultsBox;

    private RMIClient rmiClient;
    private Runnable onSearchByName;
    private Runnable onProfileClick;
    private Consumer<SearchResult> onResultClick;

    public void initServices(RMIClient rmiClient) {
        this.rmiClient = rmiClient;
        loadProfileImage();
    }

    private void loadProfileImage() {
        if (rmiClient != null) {
            Task<Image> task = new Task<>() {
                @Override
                protected Image call() throws Exception {
                    return rmiClient.getImage("images/profile/default.png");
                }
            };

            task.setOnSucceeded(e -> {
                Image image = task.getValue();
                if (image != null) {
                    Platform.runLater(() -> {
                        profileImage.setImage(image);
                        profileImage.setFitHeight(40);
                        profileImage.setFitWidth(40);
                        profileImage.setPreserveRatio(true);
                    });
                }
            });

            task.setOnFailed(e -> {
                Platform.runLater(() -> 
                    UiComponent.showError("Error", "Failed to load profile image: " + task.getException().getMessage())
                );
            });

            new Thread(task).start();
        }
    }

    private void handleSearchByName() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            searchResultsBox.setVisible(false);
            return;
        }

        if (onSearchByName != null) {
            onSearchByName.run();
        }
    }

    private void handleProfileClick() {
        if (rmiClient != null) {
            try {
                User user = rmiClient.getUserById();
                if (user != null) {
                    // Show user profile dialog
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("User Profile");
                    alert.setHeaderText("Profile Information");
                    alert.setContentText(
                        "Username: " + user.getUsername() + "\n" +
                        "Email: " + user.getEmail() + "\n" +
                        "Role: " + (rmiClient.isAdmin(user.getId()) ? "Admin" : "User")
                    );
                    alert.showAndWait();
                } else {
                    UiComponent.showError("Error", "Not logged in");
                }
            } catch (Exception e) {
                UiComponent.showError("Error", "Failed to get user profile: " + e.getMessage());
            }
        }
    }

    public static class SearchResult {
        private final String title;
        private final String subtitle;
        private final String type; // "album", "artist", or "song"
        private final int id;
        private final String imageUrl;

        public SearchResult(String title, String subtitle, String type, int id, String imageUrl) {
            this.title = title;
            this.subtitle = subtitle;
            this.type = type;
            this.id = id;
            this.imageUrl = imageUrl;
        }

        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public String getType() { return type; }
        public int getId() { return id; }
        public String getImageUrl() { return imageUrl; }
    }

    @FXML
    public void initialize() {
        setupEventHandlers();
        setupSearchResults();
    }

    private void setupEventHandlers() {
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleSearchByName();
            }
        });
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() >= 2) {
                handleSearchByName();
            } else {
                searchResultsBox.setVisible(false);
            }
        });
        profileImage.setOnMouseClicked(e -> handleProfileClick());
    }

    private void setupSearchResults() {
        searchResultsBox.setVisible(false);
        searchResultsBox.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        searchResultsBox.setMaxHeight(300);
    }

    public void setOnSearchByName(Runnable callback) {
        this.onSearchByName = callback;
    }

    public void setOnProfileClick(Runnable callback) {
        this.onProfileClick = callback;
    }

    public void setOnResultClick(Consumer<SearchResult> callback) {
        this.onResultClick = callback;
    }

    public void updateSearchResults(List<SearchResult> results) {
        searchResultsBox.getChildren().clear();
        
        if (results.isEmpty()) {
            Label noResults = new Label("No results found");
            noResults.setStyle("-fx-text-fill: gray; -fx-padding: 10;");
            searchResultsBox.getChildren().add(noResults);
        } else {
            for (SearchResult result : results) {
                HBox resultItem = createSearchResultItem(result);
                searchResultsBox.getChildren().add(resultItem);
            }
        }
        
        searchResultsBox.setVisible(true);
    }

    private HBox createSearchResultItem(SearchResult result) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 10; -fx-cursor: hand;");
        item.setOnMouseClicked(e -> {
            if (onResultClick != null) {
                onResultClick.accept(result);
            }
            searchResultsBox.setVisible(false);
        });

        ImageView imageView = new ImageView();
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setPreserveRatio(true);

        // Load image asynchronously
        Task<Image> imageTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                return rmiClient.getImage(result.getImageUrl());
            }
        };

        imageTask.setOnSucceeded(e -> {
            Image image = imageTask.getValue();
            if (image != null) {
                imageView.setImage(image);
            }
        });

        new Thread(imageTask).start();

        VBox textBox = new VBox(2);
        Label titleLabel = new Label(result.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold;");
        Label subtitleLabel = new Label(result.getSubtitle());
        subtitleLabel.setStyle("-fx-text-fill: gray;");
        textBox.getChildren().addAll(titleLabel, subtitleLabel);

        item.getChildren().addAll(imageView, textBox);
        return item;
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

    public TextField getSearchField() {
        return searchField;
    }
} 