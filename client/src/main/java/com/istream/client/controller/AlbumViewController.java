package com.istream.client.controller;

import java.util.List;
import java.util.Collections;
import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import com.istream.client.util.ThreadManager;
import com.istream.model.Album;
import com.istream.model.Song;
import com.istream.model.Artist;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.concurrent.Task;

public class AlbumViewController {
    @FXML private ImageView albumCover;
    @FXML private Label albumName;
    @FXML private Label artistName;
    @FXML private Label albumYear;
    @FXML private Label songCount;
    @FXML private Label totalDuration;
    @FXML private VBox songsBox;
    @FXML private VBox contentPane;
    @FXML private Button playButton;
    @FXML private Button shuffleButton;
    @FXML private Button addToQueueButton;
    @FXML private ScrollPane scrollPane;

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
        setupScrollPane();
        setupActionButtons();
        loadAlbumDetails();
        loadAlbumSongs();
    }

    private void setupScrollPane() {
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("smooth-scroll");
    }

    private void setupActionButtons() {
        playButton.setOnAction(e -> playAlbum(false));
        shuffleButton.setOnAction(e -> playAlbum(true));
        addToQueueButton.setOnAction(e -> addAlbumToQueue());
    }

    private void loadAlbumDetails() {
        Task<Album> albumTask = new Task<>() {
            @Override
            protected Album call() throws Exception {
                return rmiClient.getAlbumById(albumId);
            }
        };

        albumTask.setOnSucceeded(e -> {
            Album album = albumTask.getValue();
            ThreadManager.runOnFxThread(() -> {
                albumName.setText(album.getTitle());
                artistName.setText(getArtistName(album.getArtistId()));
                albumYear.setText(String.valueOf(2002));
                UiComponent.loadImage(albumCover, "images/albumCover/" + album.getId() + ".png", rmiClient);
            });
        });

        ThreadManager.submitTask(albumTask);
    }

    private String getArtistName(int artistId) {
        try {
            Artist artist = rmiClient.getArtistById(artistId);
            return artist != null ? artist.getName() : "Unknown Artist";
        } catch (Exception e) {
            return "Unknown Artist";
        }
    }

    private void loadAlbumSongs() {
        Task<List<Song>> songsTask = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                System.out.println("Fetching songs for album ID: " + albumId);
                return rmiClient.getSongsByAlbumId(albumId);
            }
        };

        songsTask.setOnSucceeded(e -> {
            List<Song> songs = songsTask.getValue();
            ThreadManager.runOnFxThread(() -> {
                songCount.setText(songs.size() + " songs");
                totalDuration.setText(formatTotalDuration(songs));
                UiComponent.createAlbumSongList(songs, songsBox, rmiClient, mainAppController);
            });
        });

        ThreadManager.submitTask(songsTask);
    }

    private String formatTotalDuration(List<Song> songs) {
        int totalSeconds = songs.stream().mapToInt(Song::getDuration).sum();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d min %02d sec", minutes, seconds);
    }

    private void playAlbum(boolean shuffle) {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return rmiClient.getSongsByAlbumId(albumId);
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            if (shuffle) {
                Collections.shuffle(songs);
            }
            if (!songs.isEmpty()) {
                mainAppController.playSong(songs.get(0));
                for (int i = 1; i < songs.size(); i++) {
                    mainAppController.addToQueue(songs.get(i));
                }
            }
        });

        ThreadManager.submitTask(task);
    }

    private void addAlbumToQueue() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return rmiClient.getSongsByAlbumId(albumId);
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            //songs.forEach(mainAppController::addToQueue);
            for (Song song: songs) {
                UiComponent.addSongToQueueAsync(song, rmiClient, mainAppController);
            }
            UiComponent.showNotification("Added to Queue", 
                songs.size() + " songs from " + albumName.getText() + " added to queue");
        });

        ThreadManager.submitTask(task);
    }
}