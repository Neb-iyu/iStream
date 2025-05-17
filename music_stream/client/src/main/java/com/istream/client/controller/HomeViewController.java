package com.istream.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.istream.client.service.AudioService;
import com.istream.rmi.MusicService;
import com.istream.model.Song;
import com.istream.model.PlayHistory;
import com.istream.model.Artist;
import com.istream.model.Album;
import com.istream.client.util.UiComponent;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;
import javafx.geometry.Pos;

public class HomeViewController {
    @FXML private VBox listenAgainContainer;
    @FXML private VBox artistsContainer;
    @FXML private VBox albumsContainer;
    @FXML private HBox listenAgainBox;
    @FXML private HBox artistsBox;
    @FXML private HBox albumsBox;
    @FXML private ScrollPane listenAgainScrollPane;
    @FXML private ScrollPane artistsScrollPane;
    @FXML private ScrollPane albumsScrollPane;
    
    private final MusicService musicService;
    private final int userId;
    private final AudioService audioService;

    public HomeViewController(MusicService musicService, int userId) {
        this.musicService = musicService;
        this.userId = userId;
        this.audioService = new AudioService();
    }
    
    @FXML
    public void initialize() {
        // Configure scroll pane behavior
        UiComponent.configureScrollPane(listenAgainScrollPane);
        UiComponent.configureScrollPane(artistsScrollPane);
        UiComponent.configureScrollPane(albumsScrollPane);
        
        loadListenAgainSongs();
        loadArtists();
        loadAlbums();
    }

    private void loadListenAgainSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                List<PlayHistory> history = musicService.getHistory(userId);
                List<Integer> songIds = history.stream()    
                    .map(PlayHistory::getSongId)
                    .collect(Collectors.toList());
                List<Song> songs = new ArrayList<>();
                for (int songId : songIds) {
                    songs.add(musicService.getSongById(songId));
                }
                return songs;
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            if (songs != null && !songs.isEmpty()) {
                listenAgainBox.getChildren().clear();
                for (Song song : songs) {
                    VBox songBox = UiComponent.createSongBox(song);
                    listenAgainBox.getChildren().add(songBox);
                }
            } else {
                UiComponent.showInfo(null, "No recent songs to display");
            }
        });

        task.setOnFailed(e -> {
            UiComponent.showError(null, "Error loading listen again songs: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void loadArtists() {
        Task<List<Artist>> task = new Task<>() {
            @Override
            protected List<Artist> call() throws Exception {
                return musicService.getAllArtists();
            }
        };

        task.setOnSucceeded(e -> {
            List<Artist> artists = task.getValue();
            if (artists != null && !artists.isEmpty()) {
                artistsBox.getChildren().clear();
                for (Artist artist : artists) {
                    VBox artistBox = UiComponent.createArtistBox(artist);
                    artistsBox.getChildren().add(artistBox);
                }
            } else {
                UiComponent.showInfo(null,"No artists to display");
            }
        });
        
    }

    private void loadAlbums() {
        Task<List<Album>> task = new Task<>() {
            @Override
            protected List<Album> call() throws Exception {
                return musicService.getAllAlbums();
            }
        };

        task.setOnSucceeded(e -> {
            List<Album> albums = task.getValue();
            if (albums != null && !albums.isEmpty()) {
                albumsBox.getChildren().clear();
                for (Album album : albums) {
                    VBox albumBox = UiComponent.createAlbumBox(album);
                    albumsBox.getChildren().add(albumBox);
                }
            } else {
                UiComponent.showInfo(null, "No albums to display");
            }
        });

        new Thread(task).start();
    }


}
