package com.istream.client.controller;

import java.util.List;
import java.util.function.Consumer;

import com.istream.client.service.RMIClient;
import com.istream.client.service.SearchService;
import com.istream.client.util.ThreadManager;
import com.istream.client.util.UiComponent;
import com.istream.model.User;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

public class TopMenuController {
    @FXML private HBox root;
    @FXML private Pane searchPane;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ImageView profileImage;
    @FXML private Pane leftPane;

    private VBox searchResultsBox;
    private RMIClient rmiClient;
    private Runnable onSearchByName;
    private Runnable onProfileClick;
    private Consumer<SearchResult> onResultClick;
    private SearchService searchService;
    private Popup searchResultsPopup;

    public void initServices(RMIClient rmiClient, MainAppController mainAppController) {
        this.rmiClient = rmiClient;
        this.searchService = new SearchService(rmiClient, this, mainAppController);
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
            searchResultsPopup.hide();
            return;
        }
        searchResultsBox.getChildren().clear();
        Label loadingLabel = new Label("Searching...");
        loadingLabel.setStyle("-fx-text-fill: gray; -fx-padding: 10;");
        searchResultsBox.getChildren().add(loadingLabel);

        showSearchResultsPopup(); 

        if (onSearchByName != null) {
            onSearchByName.run();
        }
    }

    private void handleProfileClick() {
        if (rmiClient != null) {
            try {
                User user = rmiClient.getCurrentUser();
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

        // Create popup and VBox for results
        searchResultsBox = new VBox();
        searchResultsBox.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.15),8,0,0,2);");
        searchResultsBox.setMaxHeight(300);

        searchResultsPopup = new Popup();
        searchResultsPopup.setAutoHide(true);
        searchResultsPopup.getContent().add(searchResultsBox);
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
                searchResultsPopup.hide(); 
            }
        });
        profileImage.setOnMouseClicked(e -> handleProfileClick());
    }

    private void setupSearchResults() {
        searchResultsBox.setVisible(true);
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

        showSearchResultsPopup(); 
    }

    private void showSearchResultsPopup() {
        Window window = searchField.getScene().getWindow();
        double x = window.getX() + searchField.localToScene(0, 0).getX() + searchField.getScene().getX();
        double y = window.getY() + searchField.localToScene(0, 0).getY() + searchField.getScene().getY() + searchField.getHeight();
        searchResultsPopup.show(window, x, y);
        searchResultsBox.setPrefWidth(searchField.getWidth());
    }

    private HBox createSearchResultItem(SearchResult result) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 10; -fx-cursor: hand;");
        item.setOnMouseClicked(e -> {
            if (onResultClick != null) {
                onResultClick.accept(result);
            }
            searchResultsPopup.hide(); 
        });

        ImageView imageView = new ImageView();
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setPreserveRatio(true);
        UiComponent.loadImage(imageView, result.getImageUrl(), rmiClient);

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

        ThreadManager.submitTask(imageTask);

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