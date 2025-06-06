package com.istream.client.controller;

import java.util.List;

import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import com.istream.model.Song;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

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
            Platform.runLater(() -> {
                if (likedSongsBox == null) {
                    System.err.println("Warning: likedSongsBox is null");
                    return;
                }
                
                if (songs == null || songs.isEmpty()) {
                    UiComponent.createEmptyLikedSongsMessage(likedSongsBox);
                    return;
                }

                UiComponent.createSongRow(songs, likedSongsBox, rmiClient, mainAppController);
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load liked songs: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }
}