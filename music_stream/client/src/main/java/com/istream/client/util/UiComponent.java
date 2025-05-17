package com.istream.client.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import com.istream.model.Song;
import com.istream.model.Artist;
import com.istream.model.Album;
import com.istream.client.view.*;
import com.istream.client.service.AudioService;
import com.istream.rmi.MusicService;
import com.istream.client.controller.MainAppController;
import javafx.concurrent.Task;

public class UiComponent {
    public static void showError(String header, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String header, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static VBox createSongBox(Song song) {
        VBox songBox = new VBox(5);
        songBox.setAlignment(Pos.CENTER);
        songBox.setMinWidth(150); // Set minimum width for consistent sizing
        
        try {
            ImageView imageView = new ImageView(song.getCoverArtPath());
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            
            Button titleButton = new Button(song.getTitle());
            titleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            titleButton.setOnAction(e -> {
                audioService.playStream(musicService.streamSong(song.getId()));
            });
            
            songBox.getChildren().addAll(imageView, titleButton);
        } catch (Exception ex) {
            System.err.println("Error loading song: " + song.getTitle() + " - " + ex.getMessage());
        }
        
        return songBox;
    }

    public static VBox createArtistBox(Artist artist, MainAppController mainAppController) {
        VBox artistBox = new VBox(5);
        artistBox.setAlignment(Pos.CENTER);
        artistBox.setMinWidth(150);

        try {
            ImageView imageView = new ImageView(artist.getImageUrl());
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            
            Button titleButton = new Button(artist.getName());
            titleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            titleButton.setOnAction(e -> {
                // TODO: Implement artist details view
                Task<Void> task = new Task<>() {    
                    @Override
                    protected Void call() throws Exception {
                        ArtistView artistView = new ArtistView(musicService, userId, artist.getId(), mainAppController);
                        mainAppController.setContent(artistView);
                        return null;
                    }
                };
                task.setOnFailed(fail -> {
                    showError(null, "Error loading artist: " + artist.getName() + " - " + fail.getException().getMessage());
                });
                new Thread(task).start();
            });
            
            artistBox.getChildren().addAll(imageView, titleButton);
        } catch (Exception ex) {
            System.err.println("Error loading artist: " + artist.getName() + " - " + ex.getMessage());
        }
        
        return artistBox;
    }

    public static VBox createAlbumBox(Album album, MainAppController mainAppController) {
        VBox albumBox = new VBox(5);
        albumBox.setAlignment(Pos.CENTER);
        albumBox.setMinWidth(150);
        
        try {
            ImageView imageView = new ImageView(album.getCoverArtPath());
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            
            Button titleButton = new Button(album.getTitle());
            titleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            titleButton.setOnAction(e -> {
                // TODO: Implement album details view
                Task <Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        AlbumView albumView = new AlbumView(musicService, userId, album.getId(), mainAppController);
                        mainAppController.setContent(albumView);
                        return null;
                    }
                };
                task.setOnFailed(fail -> {
                    showError(null, "Error loading album: " + album.getTitle() + " - " + fail.getException().getMessage());
                });
                new Thread(task).start();
            });
            
            albumBox.getChildren().addAll(imageView, titleButton);
        } catch (Exception ex) {
            System.err.println("Error loading album: " + album.getTitle() + " - " + ex.getMessage());
        }
        
        return albumBox;
    }

    public static ScrollPane configureScrollPane(ScrollPane scrollPane) {
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }
}
