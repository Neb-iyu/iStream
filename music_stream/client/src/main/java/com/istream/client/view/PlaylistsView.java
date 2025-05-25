package com.istream.client.view;

import com.istream.client.controller.MainAppController;
import com.istream.client.service.RMIClient;
import com.istream.model.Playlist;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.concurrent.Task;
import javafx.application.Platform;
import java.util.List;
import java.util.ArrayList;

public class PlaylistsView extends ScrollPane {
    private final RMIClient rmiClient;
    private final MainAppController mainAppController;
    private final GridPane gridPane;

    public PlaylistsView(RMIClient rmiClient, MainAppController mainAppController) {
        this.rmiClient = rmiClient;
        this.mainAppController = mainAppController;

        setFitToWidth(true);
        setFitToHeight(true);
        getStyleClass().add("scroll-pane");

        // Main content
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("vbox");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("hbox");

        Label titleLabel = new Label("Your Playlists");
        titleLabel.getStyleClass().add("title");

        Button createButton = new Button("Create New Playlist");
        createButton.getStyleClass().add("primary");
        createButton.setOnAction(e -> handleCreatePlaylist());

        header.getChildren().addAll(titleLabel, createButton);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Grid for playlists
        gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.getStyleClass().add("grid-pane");

        content.getChildren().addAll(header, gridPane);
        setContent(content);

        loadPlaylists();
    }

    private void loadPlaylists() {
        Task<List<Playlist>> task = new Task<>() {
            @Override
            protected List<Playlist> call() throws Exception {
                return rmiClient.getUserPlaylists();
            }
        };

        task.setOnSucceeded(e -> {
            List<Playlist> playlists = task.getValue();
            gridPane.getChildren().clear();
            
            int col = 0;
            int row = 0;
            for (Playlist playlist : playlists) {
                VBox playlistCard = createPlaylistCard(playlist);
                gridPane.add(playlistCard, col, row);
                
                col++;
                if (col > 3) {
                    col = 0;
                    row++;
                }
            }
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to load playlists");
                alert.setContentText(task.getException().getMessage());
                alert.showAndWait();
            });
        });

        new Thread(task).start();
    }

    private VBox createPlaylistCard(Playlist playlist) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("card");
        card.setOnMouseClicked(e -> {
            PlaylistView playlistView = new PlaylistView(rmiClient, mainAppController, playlist);
            mainAppController.setContent(playlistView);
        });

        // Playlist cover
        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(200);
        coverImage.setFitHeight(200);
        coverImage.setPreserveRatio(true);
        coverImage.getStyleClass().add("image-view");
        try {
            Image image = rmiClient.getImage("images/playlist/default.png");
            if (image != null) {
                coverImage.setImage(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Playlist info
        Label nameLabel = new Label(playlist.getName());
        nameLabel.getStyleClass().add("title");
        nameLabel.setWrapText(true);

        Label songsLabel = new Label(playlist.getSongs().size() + " songs");
        songsLabel.getStyleClass().add("subtitle");

        card.getChildren().addAll(coverImage, nameLabel, songsLabel);
        return card;
    }

    private void handleCreatePlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Playlist");
        dialog.setHeaderText("Enter playlist name");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        rmiClient.createPlaylist(name.trim());
                        return null;
                    }
                };

                task.setOnSucceeded(e -> loadPlaylists());

                task.setOnFailed(e -> {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Failed to create playlist");
                        alert.setContentText(task.getException().getMessage());
                        alert.showAndWait();
                    });
                });

                new Thread(task).start();
            }
        });
    }
} 