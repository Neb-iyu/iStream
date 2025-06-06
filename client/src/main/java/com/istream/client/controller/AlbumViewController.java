package com.istream.client.controller;

import java.util.List;

import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import com.istream.client.util.ThreadManager;
import com.istream.model.Album;
import com.istream.model.Song;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.concurrent.Task;

public class AlbumViewController {
    @FXML private ImageView albumCover;
    @FXML private Label albumName;
    @FXML private Label artistName;
    @FXML private HBox songsBox;

    private final RMIClient rmiClient;
    private final int albumId;
    private final MainAppController mainAppController;

    public AlbumViewController(RMIClient rmiClient, int albumId, MainAppController mainAppController) {
        this.rmiClient = rmiClient;
        this.albumId = albumId;
        this.mainAppController = mainAppController;
    }

    @FXML
    public void initialize() {
        loadAlbumDetails();
        loadAlbumSongs();
    }

    private void loadAlbumDetails() {
        Task<Album> task = new Task<>() {
            @Override
            protected Album call() throws Exception {
                return rmiClient.getAlbumById(albumId);
            }
        };

        task.setOnSucceeded(e -> {
            Album album = task.getValue();
            ThreadManager.runOnFxThread(() -> {
                albumName.setText(album.getTitle());
                artistName.setText(String.valueOf(album.getArtistId()));
                UiComponent.loadImage(albumCover, "images/albumCover/" + album.getId() + ".png", rmiClient);
            });
        });

        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> 
                UiComponent.showError("Error", "Failed to load album details: " + task.getException().getMessage())
            );
        });

        ThreadManager.submitTask(task);
    }

    private void loadAlbumSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return rmiClient.getSongsByAlbumId(albumId);
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            ThreadManager.runOnFxThread(() -> {
                UiComponent.createSongRow(songs, songsBox, rmiClient, mainAppController);
            });
        });

        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> 
                UiComponent.showError("Error", "Failed to load album songs: " + task.getException().getMessage())
            );
        });

        ThreadManager.submitTask(task);
    }
}
