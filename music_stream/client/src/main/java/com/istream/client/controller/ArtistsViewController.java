package com.istream.client.controller;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.istream.rmi.MusicService;
import com.istream.model.Artist;
import com.istream.model.Song;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;
import javafx.geometry.Pos;

public class ArtistsViewController {
    @FXML private VBox artistsContainer;
    @FXML private HBox artistsBox;
    @FXML private ScrollPane artistsScrollPane;
    
    private final MusicService musicService;
    private final int userId;
    private final ExecutorService executorService;

    public ArtistsViewController(MusicService musicService, int userId) {
        this.musicService = musicService;
        this.userId = userId;
        this.executorService = Executors.newFixedThreadPool(2); // One for loading artists, one for loading songs
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
        // Use JavaFX Task since this operation updates the UI
        Task<List<Artist>> loadTask = new Task<>() {
            @Override
            protected List<Artist> call() throws Exception {
                return musicService.getAllArtists();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Artist> artists = loadTask.getValue();
            if (artists != null && !artists.isEmpty()) {
                artistsBox.getChildren().clear();
                for (Artist artist : artists) {
                    VBox artistBox = createArtistBox(artist);
                    artistsBox.getChildren().add(artistBox);
                }
            } else {
                showInfoAlert("No artists to display");
            }
        });

        loadTask.setOnFailed(e -> {
            showErrorAlert("Error loading artists: " + loadTask.getException().getMessage());
        });

        // Use JavaFX's thread pool for UI-related tasks
        javafx.concurrent.Service<Void> service = new javafx.concurrent.Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        loadTask.run();
                        return null;
                    }
                };
            }
        };
        service.start();
    }

    private VBox createArtistBox(Artist artist) {
        VBox artistBox = new VBox(5);
        artistBox.setAlignment(Pos.CENTER);
        artistBox.setMinWidth(150);
        
        try {
            ImageView imageView = new ImageView(artist.getImagePath());
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
                        return musicService.getSongsByArtist(artist.getId());
                    }
                };

                loadSongsTask.setOnSucceeded(success -> {
                    List<Song> songs = loadSongsTask.getValue();
                    // TODO: Show artist songs in a new view or dialog
                    showInfoAlert("Found " + songs.size() + " songs by " + artist.getName());
                });

                loadSongsTask.setOnFailed(fail -> {
                    showErrorAlert("Error loading artist songs: " + loadSongsTask.getException().getMessage());
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

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        executorService.shutdown();
    }
} 