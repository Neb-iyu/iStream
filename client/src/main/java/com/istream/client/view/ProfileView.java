package com.istream.client.view;

import com.istream.client.controller.MainAppController;
import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import com.istream.client.util.SessionHolder;
import com.istream.client.ClientMain;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.concurrent.Task;
import javafx.application.Platform;
import java.io.IOException;
import javafx.scene.Parent;
import com.istream.client.controller.AuthController;

public class ProfileView extends StackPane {
    private final RMIClient rmiClient;
    private final MainAppController mainAppController;
    private final SessionHolder sessionHolder;
    private ImageView profileImage;
    private Label usernameLabel;
    private Button logoutButton;

    public ProfileView(RMIClient rmiClient, MainAppController mainAppController) throws IOException {
        this.rmiClient = rmiClient;
        this.mainAppController = mainAppController;
        this.sessionHolder = new SessionHolder(rmiClient);
        
        setupUI();
        loadUserProfile();
    }

    private void setupUI() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #121212; -fx-padding: 20;");

        // Profile Image
        profileImage = new ImageView();
        profileImage.setFitHeight(150);
        profileImage.setFitWidth(150);
        profileImage.setPreserveRatio(true);
        profileImage.setStyle("-fx-background-color: #2c2c2c; -fx-background-radius: 75;");

        // Username Label
        usernameLabel = new Label();
        usernameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Logout Button
        logoutButton = new Button("Logout");
        logoutButton.setStyle(
            "-fx-background-color: #1db954; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );
        logoutButton.setOnAction(e -> handleLogout());

        // Add hover effect to logout button
        logoutButton.setOnMouseEntered(e -> 
            logoutButton.setStyle(
                "-fx-background-color: #1ed760; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 5;"
            )
        );
        logoutButton.setOnMouseExited(e -> 
            logoutButton.setStyle(
                "-fx-background-color: #1db954; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 5;"
            )
        );

        container.getChildren().addAll(profileImage, usernameLabel, logoutButton);
        getChildren().add(container);
    }

    private void loadUserProfile() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String username = sessionHolder.getUsername();
                if (username != null) {
                    Platform.runLater(() -> {
                        usernameLabel.setText(username);
                        try {
                            // Load profile image
                            Image image = rmiClient.getImage("images/profile/default.png");
                            if (image != null) {
                                profileImage.setImage(image);
                            }
                        } catch (Exception ex) {
                            UiComponent.showError("Error", "Failed to load profile image: " + ex.getMessage());
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        UiComponent.showError("Error", "Not logged in");
                        try {
                            mainAppController.setContent(new HomeView(rmiClient, mainAppController));
                        } catch (IOException ex) {
                            UiComponent.showError("Error", "Failed to load home view: " + ex.getMessage());
                        }
                    });
                }
                return null;
            }
        };

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                UiComponent.showError("Error", "Failed to load profile: " + task.getException().getMessage());
                try {
                    mainAppController.setContent(new HomeView(rmiClient, mainAppController));
                } catch (IOException ex) {
                    UiComponent.showError("Error", "Failed to load home view: " + ex.getMessage());
                }
            });
        });

        new Thread(task).start();
    }

    private void handleLogout() {
        try {
            rmiClient.logout();
            sessionHolder.clearSession();
            ClientMain.getInstance().showLoginScreen();
        } catch (Exception e) {
            UiComponent.showError("Error", "Failed to logout: " + e.getMessage());
        }
    }
} 