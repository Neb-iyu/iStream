package com.istream.client.controller;

import java.util.List;

import com.istream.client.service.RMIClient;
import com.istream.client.util.ThreadManager;
import com.istream.client.util.UiComponent;
import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.Song;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class ArtistViewController {
    @FXML private VBox root;
    @FXML private ImageView artistImage;
    @FXML private ImageView headerBackground;
    @FXML private Label artistName;
    @FXML private Label songCountLabel;
    @FXML private Label monthlyListenersLabel;
    @FXML private Label bioLabel;
    @FXML private VBox songsContainer;
    @FXML private HBox albumsContainer;
    @FXML private Button playAll;
    
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
        setupArtistImageClip();
        loadArtistData();
        playAll.setOnAction(e -> playArtistSongs());
    }

    private void setupArtistImageClip() {
        Rectangle clip = new Rectangle(200, 200);
        clip.setArcWidth(200);
        clip.setArcHeight(200);
        artistImage.setClip(clip);
    }

    private void playArtistSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                Artist artist = rmiClient.getArtistById(artistId);
                return artist != null ? artist.getSongs() : List.of();
            }
        };
        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            if (!songs.isEmpty()) 
                mainAppController.playSongs(songs);
        });

        ThreadManager.submitTask(task);
    }
 


    private void loadArtistData() {

        Task<Artist> artistTask = new Task<>() {
            @Override
            protected Artist call() throws Exception {
                return rmiClient.getArtistById(artistId);
            }
        };

        artistTask.setOnSucceeded(e -> {
            Artist artist = artistTask.getValue();
            if (artist != null) {
                updateArtistUI(artist);
                loadArtistSongs();
                loadArtistAlbums();
            }
        });

        ThreadManager.submitTask(artistTask);
    }

    private void updateArtistUI(Artist artist) {
        Platform.runLater(() -> {
            artistName.setText(artist.getName());
            songCountLabel.setText(artist.getSongs().size() + " songs");
            monthlyListenersLabel.setText("0 monthly listeners"); // Placeholder since model doesn't have this
            bioLabel.setText("No biography available"); // Placeholder since model doesn't have this
            
            UiComponent.loadImage(artistImage, 
                "images/artist/" + artist.getId() + ".png", 
                rmiClient
            );
            UiComponent.loadImage(headerBackground,
                "images/artist/header_" + artist.getId() + ".jpg",
                rmiClient
            );
        });
    }

    private String formatNumber(int number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return Integer.toString(number);
    }

    private void loadArtistSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                Artist artist = rmiClient.getArtistById(artistId);
                return artist != null ? artist.getSongs() : List.of();
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            Platform.runLater(() -> {
                songsContainer.getChildren().clear();
                songs.forEach(song -> {
                    HBox songItem = createSongItem(song);
                    songsContainer.getChildren().add(songItem);
                });
            });
        });

        ThreadManager.submitTask(task);
    }

    private HBox createSongItem(Song song) {
         HBox item = new HBox(10);
        item.getStyleClass().add("song-item");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 16, 8, 16));

        Label titleLabel = new Label(song.getTitle());
        titleLabel.getStyleClass().add("song-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button playButton = new Button("▶");
        playButton.getStyleClass().add("song-action-button");
        playButton.setOnAction(e -> mainAppController.playSong(song));

        Label durationLabel = new Label(formatDuration(song.getDuration()));
        durationLabel.getStyleClass().add("song-duration");

        item.getChildren().addAll(playButton, titleLabel, durationLabel);

        item.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                mainAppController.playSong(song);
            }
        });

        return item;
    }

    private String formatDuration(int seconds) {
    int minutes = seconds / 60;
    int remainingSeconds = seconds % 60;
    return String.format("%d:%02d", minutes, remainingSeconds);
}

    private void loadArtistAlbums() {
        Task<List<Album>> task = new Task<>() {
            @Override
            protected List<Album> call() throws Exception {
                Artist artist = rmiClient.getArtistById(artistId);
                return artist != null ? artist.getAlbums() : List.of();
            }
        };

        task.setOnSucceeded(e -> {
            List<Album> albums = task.getValue();
            Platform.runLater(() -> {
                albumsContainer.getChildren().clear();
                albums.forEach(album -> {
                    VBox albumItem = createAlbumItem(album);
                    albumsContainer.getChildren().add(albumItem);
                });
            });
        });

        ThreadManager.submitTask(task);
    }

    private VBox createAlbumItem(Album album) {
        VBox item = new VBox();
        item.getStyleClass().add("album-item");
        
        Label nameLabel = new Label(album.getTitle());
        nameLabel.getStyleClass().add("album-title");
        
        item.getChildren().add(nameLabel);
        return item;
    }
}