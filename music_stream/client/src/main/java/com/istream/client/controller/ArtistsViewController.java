package com.istream.client.controller;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.rmi.RemoteException;
import com.istream.rmi.MusicService;
import com.istream.model.Artist;
import com.istream.model.Song;
import com.istream.client.util.UiComponent;
import com.istream.client.service.RMIClient;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.application.Platform;

public class ArtistsViewController {
    @FXML private VBox artistsContainer;
    @FXML private HBox artistsBox;
    @FXML private ScrollPane artistsScrollPane;
    
    private final RMIClient rmiClient;
    private final MainAppController mainAppController;

    public ArtistsViewController(RMIClient rmiClient, MainAppController mainAppController) {
        this.rmiClient = rmiClient;
        this.mainAppController = mainAppController;
    }
    
    @FXML
    public void initialize() {
        // Configure scroll pane behavior
        artistsScrollPane.setFitToHeight(true);
        artistsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        artistsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        artistsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        loadArtists();
    }

    private void loadArtists() {
        Task<List<Artist>> task = new Task<>() {
            @Override
            protected List<Artist> call() throws Exception {
                return rmiClient.getAllArtists();
            }
        };

        task.setOnSucceeded(e -> {
            List<Artist> artists = task.getValue();
            UiComponent.createArtistRow(artists, artistsBox, rmiClient, mainAppController);
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load artists: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }

    private VBox createArtistBox(Artist artist, MainAppController mainAppController) {
        VBox artistBox = new VBox(5);
        artistBox.setAlignment(Pos.CENTER);
        artistBox.setMinWidth(150);
        
        try {
            ImageView imageView = new ImageView(artist.getImageUrl());
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            
            Button nameButton = new Button(artist.getName());
            nameButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            nameButton.setOnAction(e -> {
                // Use JavaFX Task for UI updates when loading songs
                Task<List<Song>> loadSongsTask = new Task<>() {
                    @Override
                    protected List<Song> call() throws Exception {
                        return rmiClient.getSongsByArtistId(artist.getId());
                    }
                };

                loadSongsTask.setOnSucceeded(success -> {
                    List<Song> songs = loadSongsTask.getValue();
                    // TODO: Show artist songs in a new view or dialog
                    UiComponent.showInputDialog(null, "Found " + songs.size() + " songs by " + artist.getName());
                });

                loadSongsTask.setOnFailed(fail -> {
                    UiComponent.showError(null, "Error loading artist songs: " + loadSongsTask.getException().getMessage());
                });

                // Use JavaFX's thread pool for UI-related tasks
                javafx.concurrent.Service<Void> service = new javafx.concurrent.Service<>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<>() {
                            @Override
                            protected Void call() {
                                loadSongsTask.run();
                                return null;
                            }
                        };
                    }
                };
                service.start();
            });
            
            artistBox.getChildren().addAll(imageView, nameButton);
        } catch (Exception ex) {
            System.err.println("Error loading artist: " + artist.getName() + " - " + ex.getMessage());
        }
        
        return artistBox;
    }

    
    public void cleanup() {
        // executorService.shutdown();
    }
} 