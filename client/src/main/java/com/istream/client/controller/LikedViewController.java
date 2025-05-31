package com.istream.client.controller;

import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import com.istream.model.Song;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.concurrent.Task;
import javafx.application.Platform;
import java.util.List;

public class LikedViewController {
    @FXML private HBox likedSongsBox;

    private final RMIClient rmiClient;
    private final MainAppController mainAppController;

    public LikedViewController(RMIClient rmiClient, MainAppController mainAppController) {
        this.rmiClient = rmiClient;
        this.mainAppController = mainAppController;
    }

    @FXML
    public void initialize() {
        loadLikedSongs();
    }

    private void loadLikedSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return rmiClient.getLikedSongs();
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            UiComponent.createSongRow(songs, likedSongsBox, rmiClient, mainAppController);
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load liked songs: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }
}