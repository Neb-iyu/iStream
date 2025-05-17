package com.istream.client.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.istream.client.service.AudioService;
import com.istream.rmi.MusicService;
import com.istream.model.Song;
import com.istream.model.PlayHistory;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;
import javafx.geometry.Pos;

public class LikedViewController {
    @FXML private VBox likedContainer;
    @FXML private HBox likedBox;
    @FXML private ScrollPane likedScrollPane;
    
    private final MusicService musicService;
    private final int userId;
    private final AudioService audioService;

    public LikedViewController(MusicService musicService, int userId) {
        this.musicService = musicService;
        this.userId = userId;
        this.audioService = new AudioService();
    }
    
    @FXML
    public void initialize() {
        // Configure scroll pane behavior
        likedScrollPane.setFitToHeight(true);
        likedScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        likedScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        likedScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        loadLikedSongs();
    }

    private void loadLikedSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return musicService.getLikedSongs(userId);
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            if (songs != null && !songs.isEmpty()) {
                likedBox.getChildren().clear();
                for (Song song : songs) {
                    VBox songBox = createSongBox(song);
                    likedBox.getChildren().add(songBox);
                }
            } else {
                showInfoAlert("No liked songs to display");
            }
        });

        task.setOnFailed(e -> {
            showErrorAlert("Error loading liked songs: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private VBox createSongBox(Song song) {
        VBox songBox = new VBox(5);
        songBox.setAlignment(Pos.CENTER);
        songBox.setMinWidth(150);
        
        try {
            ImageView imageView = new ImageView(song.getCoverArtPath());
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            
            Button titleButton = new Button(song.getTitle());
            titleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            titleButton.setOnAction(e -> {
                try {
                    audioService.playStream(musicService.streamSong(song.getId()));
                } catch (Exception ex) {
                    showErrorAlert("Error playing song: " + ex.getMessage());
                }
            });
            
            songBox.getChildren().addAll(imageView, titleButton);
        } catch (Exception ex) {
            System.err.println("Error loading song: " + song.getTitle() + " - " + ex.getMessage());
        }
        
        return songBox;
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
} 