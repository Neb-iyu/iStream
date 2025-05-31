package com.istream.client.controller;

import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.Song;
import com.istream.client.service.RMIClient;
import com.istream.client.service.AudioService;
import com.istream.client.util.UiComponent;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.concurrent.Task;
import javafx.application.Platform;
import java.util.List;

public class HomeViewController {
    @FXML private HBox listenAgainBox;
    @FXML private HBox artistsBox;
    @FXML private HBox albumsBox;

    private final RMIClient rmiClient;
    private final MainAppController mainAppController;

    public HomeViewController(RMIClient rmiClient, MainAppController mainAppController) {
        this.rmiClient = rmiClient;
        this.mainAppController = mainAppController;
    }

    @FXML
    public void initialize() {
        loadListenAgainSongs();
        loadArtists();
        loadAlbums();
    }

    private void loadListenAgainSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return rmiClient.getHistorySongs();
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            UiComponent.createSongRow(songs, listenAgainBox, rmiClient, mainAppController);
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load recently played songs: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
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

    private void loadAlbums() {
        Task<List<Album>> task = new Task<>() {
            @Override
            protected List<Album> call() throws Exception {
                return rmiClient.getAllAlbums();
            }
        };

        task.setOnSucceeded(e -> {
            List<Album> albums = task.getValue();
            UiComponent.createAlbumRow(albums, albumsBox, rmiClient, mainAppController);
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                UiComponent.showError("Error", "Failed to load albums: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }
}
