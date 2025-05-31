package com.istream.client.view;

import com.istream.client.controller.MainAppController;
import com.istream.client.service.RMIClient;
import com.istream.model.Playlist;
import com.istream.model.Song;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.concurrent.Task;
import javafx.application.Platform;
import java.util.List;
import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import com.istream.model.Artist;
import com.istream.model.Album;

public class PlaylistView extends VBox {
    private final RMIClient rmiClient;
    private final MainAppController mainAppController;
    private final Playlist playlist;
    private final TableView<Song> songsTable;
    private Button playButton;
    private Button shuffleButton;
    private ComboBox<Song> songSearchBox;
    private ObservableList<Song> searchResults;

    public PlaylistView(RMIClient rmiClient, MainAppController mainAppController, Playlist playlist) {
        this.rmiClient = rmiClient;
        this.mainAppController = mainAppController;
        this.playlist = playlist;
        this.searchResults = FXCollections.observableArrayList();

        setSpacing(20);
        setPadding(new Insets(20));
        getStyleClass().add("vbox");

        // Header section
        HBox headerBox = createHeader();
        
        // Buttons section
        HBox buttonsBox = createButtons();
        
        // Songs table
        songsTable = createSongsTable();
        
        getChildren().addAll(headerBox, buttonsBox, songsTable);
        
        loadSongs();
    }

    private HBox createHeader() {
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("hbox");

        // Playlist cover
        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(200);
        coverImage.setFitHeight(200);
        coverImage.setPreserveRatio(true);
        coverImage.getStyleClass().add("image-view");
        try {
            Image image = rmiClient.getImage("images/playlist/default.png");
            if (image != null) {
                coverImage.setImage(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Playlist info
        VBox infoBox = new VBox(10);
        infoBox.getStyleClass().add("vbox");
        
        Label titleLabel = new Label(playlist.getName());
        titleLabel.getStyleClass().add("title");

        Label infoLabel = new Label(playlist.getSongs().size() + " songs");
        infoLabel.getStyleClass().add("subtitle");

        infoBox.getChildren().addAll(titleLabel, infoLabel);
        headerBox.getChildren().addAll(coverImage, infoBox);

        return headerBox;
    }

    private HBox createButtons() {
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);
        buttonsBox.getStyleClass().add("hbox");

        // Play button
        playButton = new Button("Play All");
        playButton.getStyleClass().add("primary");
        playButton.setOnAction(e -> handlePlayAll());

        // Shuffle button
        shuffleButton = new Button("Shuffle Play");
        shuffleButton.getStyleClass().add("button");
        shuffleButton.setOnAction(e -> handleShuffle());

        // Song search box
        songSearchBox = new ComboBox<>();
        songSearchBox.setPromptText("Search songs to add...");
        songSearchBox.setEditable(true);
        songSearchBox.setPrefWidth(300);
        songSearchBox.getStyleClass().add("combo-box");
        
        // Set up the converter to display song title and artist
        songSearchBox.setConverter(new StringConverter<Song>() {
            @Override
            public String toString(Song song) {
                if (song == null) return "";
                try {
                    Artist artist = rmiClient.getArtistById(song.getArtistId());
                    return song.getTitle() + " - " + (artist != null ? artist.getName() : "Unknown Artist");
                } catch (Exception e) {
                    return song.getTitle() + " - Unknown Artist";
                }
            }

            @Override
            public Song fromString(String string) {
                return null;
            }
        });

        // Set up the search functionality
        songSearchBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                searchSongs(newVal);
            } else {
                searchResults.clear();
                songSearchBox.setItems(searchResults);
            }
        });

        // Handle song selection
        songSearchBox.setOnAction(e -> {
            Song selectedSong = songSearchBox.getValue();
            if (selectedSong != null) {
                addSongToPlaylist(selectedSong);
                songSearchBox.setValue(null);
                songSearchBox.getEditor().clear();
            }
        });

        buttonsBox.getChildren().addAll(playButton, shuffleButton, songSearchBox);
        return buttonsBox;
    }

    private void searchSongs(String query) {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return rmiClient.searchSongs(query);
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> results = task.getValue();
            searchResults.setAll(results);
            songSearchBox.setItems(searchResults);
            songSearchBox.show();
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to search songs");
                alert.setContentText(task.getException().getMessage());
                alert.showAndWait();
            });
        });

        new Thread(task).start();
    }

    private void addSongToPlaylist(Song song) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                rmiClient.addSongToPlaylist(playlist.getId(), song.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            loadSongs(); // Reload the songs list
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to add song to playlist");
                alert.setContentText(task.getException().getMessage());
                alert.showAndWait();
            });
        });

        new Thread(task).start();
    }

    private TableView<Song> createSongsTable() {
        TableView<Song> table = new TableView<>();
        table.getStyleClass().add("table-view");

        // Title column
        TableColumn<Song, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        titleCol.setPrefWidth(300);

        // Artist column
        TableColumn<Song, String> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(cellData -> {
            try {
                Artist artist = rmiClient.getArtistById(cellData.getValue().getArtistId());
                return new SimpleStringProperty(artist != null ? artist.getName() : "Unknown Artist");
            } catch (Exception e) {
                return new SimpleStringProperty("Unknown Artist");
            }
        });
        artistCol.setPrefWidth(200);

        // Album column
        TableColumn<Song, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(cellData -> {
            try {
                Album album = rmiClient.getAlbumById(cellData.getValue().getAlbumId());
                return new SimpleStringProperty(album != null ? album.getTitle() : "Unknown Album");
            } catch (Exception e) {
                return new SimpleStringProperty("Unknown Album");
            }
        });
        albumCol.setPrefWidth(200);

        // Duration column
        TableColumn<Song, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(cellData -> {
            int duration = cellData.getValue().getDuration();
            int minutes = duration / 60;
            int seconds = duration % 60;
            return new SimpleStringProperty(String.format("%d:%02d", minutes, seconds));
        });
        durationCol.setPrefWidth(100);

        table.getColumns().addAll(titleCol, artistCol, albumCol, durationCol);
        
        // Add double-click handler
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Song selectedSong = table.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    handleSongPlay(selectedSong);
                }
            }
        });

        return table;
    }

    private void loadSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return new ArrayList<>(playlist.getSongs());
            }
        };

        task.setOnSucceeded(e -> {
            List<Song> songs = task.getValue();
            songsTable.getItems().setAll(songs);
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to load playlist songs");
                alert.setContentText(task.getException().getMessage());
                alert.showAndWait();
            });
        });

        new Thread(task).start();
    }

    private void handlePlayAll() {
        if (!songsTable.getItems().isEmpty()) {
            handleSongPlay(songsTable.getItems().get(0));
        }
    }

    private void handleShuffle() {
        if (!songsTable.getItems().isEmpty()) {
            int randomIndex = (int) (Math.random() * songsTable.getItems().size());
            handleSongPlay(songsTable.getItems().get(randomIndex));
        }
    }

    private void handleSongPlay(Song song) {
        try {
            byte[] audioData = rmiClient.streamSong(song.getId());
            mainAppController.getAudioService().playSong(song, audioData);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to play song");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}