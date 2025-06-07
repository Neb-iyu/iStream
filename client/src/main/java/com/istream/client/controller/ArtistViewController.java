package com.istream.client.controller;

import java.util.List;
import com.istream.client.service.RMIClient;
import com.istream.client.util.UiComponent;
import com.istream.client.util.ThreadManager;
import com.istream.model.Artist;
import com.istream.model.Song;
import com.istream.model.Album;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.concurrent.Task;
import javafx.scene.shape.Rectangle;
import javafx.application.Platform;

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
    }

    private void setupArtistImageClip() {
        Rectangle clip = new Rectangle(200, 200);
        clip.setArcWidth(200);
        clip.setArcHeight(200);
        artistImage.setClip(clip);
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
            
            // Load images
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

        // Song title
        Label titleLabel = new Label(song.getTitle());
        titleLabel.getStyleClass().add("song-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Play button
        Button playButton = new Button("▶");
        playButton.getStyleClass().add("song-action-button");
        playButton.setOnAction(e -> mainAppController.playSong(song));

        // Optionally, add artist name or duration
        Label durationLabel = new Label(formatDuration(song.getDuration()));
        durationLabel.getStyleClass().add("song-duration");

        item.getChildren().addAll(playButton, titleLabel, durationLabel);

        // Play song on row click (not just button)
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