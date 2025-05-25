package com.istream.client.controller;

import java.util.List;

import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import com.istream.model.Artist;
import com.istream.model.Song;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.concurrent.Task;
import javafx.application.Platform;

public class ArtistViewController {
    @FXML private ImageView artistImage;
    @FXML private Label artistName;
    @FXML private HBox songsBox;
    
    private final RMIClient rmiClient;
    private final int artistId;
    private final MainAppController mainAppController;

    public ArtistViewController(RMIClient rmiClient, int artistId, MainAppController mainAppController) {
        this.rmiClient = rmiClient;
        this.artistId = artistId;
        this.mainAppController = mainAppController;
    }
    
    @FXML
    public void initialize() {
        loadArtistDetails();
        loadArtistSongs();
    }

    private void loadArtistDetails() {
        Task<Artist> task = new Task<>() {
            @Override
            protected Artist call() throws Exception {
                return rmiClient.getArtistById(artistId);
            }
        };

        task.setOnSucceeded(e -> {
            Artist artist = task.getValue();
            artistName.setText(artist.getName());
            // TODO: Load artist image
            UiComponent.loadImage(artistImage, "images/artist/" + artist.getId() + ".png", rmiClient);
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load artist details: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }

    private void loadArtistSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return rmiClient.getSongsByArtistId(artistId);
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            UiComponent.createSongRow(songs, songsBox, rmiClient, mainAppController);
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load artist songs: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }
}
