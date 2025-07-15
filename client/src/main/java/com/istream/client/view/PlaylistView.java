package com.istream.client.view;

import com.istream.client.controller.MainAppController;
import com.istream.client.service.RMIClient;
import com.istream.client.util.ThreadManager;
import com.istream.client.util.UiComponent;
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
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import com.istream.model.Artist;
import com.istream.model.Album;
import java.util.Collections;
import java.util.Set;

public class PlaylistView extends VBox {
    private final RMIClient rmiClient;
    private final MainAppController mainAppController;
    private final Playlist playlist;
    private final TableView<Song> songsTable;
    private Button playButton;
    private Button shuffleButton;
    private ComboBox<Song> songSearchBox;
    private ObservableList<Song> searchResults;
    private PauseTransition searchDebouncer;
    private Label infoLabel; // Make this a field

    public PlaylistView(RMIClient rmiClient, MainAppController mainAppController, Playlist playlist) {
        this.rmiClient = rmiClient;
        this.mainAppController = mainAppController;
        this.playlist = playlist;
        this.searchResults = FXCollections.observableArrayList();
        this.searchDebouncer = new PauseTransition(Duration.millis(300));

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
        
        Task<Image> imageTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                return rmiClient.getImage("images/playlist/default.png");
            }
        };
        imageTask.setOnSucceeded(e -> {
            Image image = imageTask.getValue();
            if (image != null) coverImage.setImage(image);
        });
        ThreadManager.submitTask(imageTask);

        // Playlist info
        VBox infoBox = new VBox(10);
        infoBox.getStyleClass().add("vbox");

        Label titleLabel = new Label(playlist.getName());
        titleLabel.getStyleClass().add("title");

        infoLabel = new Label("Loading songs...");
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
        songSearchBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setText(null);
                } else {
                    setText(song.getTitle() + " - Loading...");
                    // Fetch artist name in background
                    Task<String> artistTask = new Task<>() {
                        @Override
                        protected String call() throws Exception {
                            Artist artist = rmiClient.getArtistById(song.getArtistId());
                            return artist != null ? artist.getName() : "Unknown Artist";
                        }
                    };
                    artistTask.setOnSucceeded(e -> setText(song.getTitle() + " - " + artistTask.getValue()));
                    ThreadManager.submitTask(artistTask);
                }
            }
        });
        songSearchBox.setPromptText("Search songs to add...");
        songSearchBox.setEditable(true);
        songSearchBox.setPrefWidth(300);
        songSearchBox.getStyleClass().add("combo-box");
        
        songSearchBox.setConverter(new StringConverter<Song>() {
            @Override
            public String toString(Song song) {
                if (song == null) return "";
                String base = song.getTitle();
                Task<String> artistTask = new Task<>() {
                    @Override
                    protected String call() throws Exception {
                        Artist artist = rmiClient.getArtistById(song.getArtistId());
                        return artist != null ? artist.getName() : "Unknown Artist";
                    }
                };
                artistTask.setOnSucceeded(e -> {
                    String display = base + " - " + artistTask.getValue();
                    Platform.runLater(() -> songSearchBox.getEditor().setText(display));
                });
                ThreadManager.submitTask(artistTask);
                return base;
            }
            @Override
            public Song fromString(String string) { return null; }
        });

        // Set up the search functionality
        songSearchBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                performSearch(newVal);
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

     private void setupSearchBox() {
        songSearchBox.setConverter(new StringConverter<Song>() {
            @Override
            public String toString(Song song) {
                if (song == null) return "";
                String base = song.getTitle();
                Task<String> artistTask = new Task<>() {
                    @Override
                    protected String call() throws Exception {
                        Artist artist = rmiClient.getArtistById(song.getArtistId());
                        return artist != null ? artist.getName() : "Unknown Artist";
                    }
                };
                artistTask.setOnSucceeded(e -> {
                    String display = base + " - " + artistTask.getValue();
                    Platform.runLater(() -> songSearchBox.getEditor().setText(display));
                });
                ThreadManager.submitTask(artistTask);
                return base;
            }
            @Override
            public Song fromString(String string) {
                return null;
            }
        });

        songSearchBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (searchDebouncer.getStatus() == javafx.animation.Animation.Status.RUNNING) {
                searchDebouncer.stop();
            }
            
            searchDebouncer.setOnFinished(e -> {
                if (newVal != null && !newVal.trim().isEmpty()) {
                    performSearch(newVal.trim());
                } else {
                    Platform.runLater(() -> {
                        searchResults.clear();
                        songSearchBox.setItems(searchResults);
                    });
                }
            });
            searchDebouncer.playFromStart();
        });

        songSearchBox.setOnAction(e -> {
            Song selectedSong = songSearchBox.getValue();
            if (selectedSong != null) {
                addSongToPlaylist(selectedSong);
                songSearchBox.setValue(null);
                songSearchBox.getEditor().clear();
            }
        });
    }

    private void performSearch(String query) {
        // Cancel any existing search
        if (searchDebouncer.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            searchDebouncer.stop();
        }

        Task<List<Song>> searchTask = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                if (query == null || query.trim().isEmpty()) {
                    return new ArrayList<>();
                }
                return rmiClient.searchSongs(query.trim());
            }
        };

        // searchTask.setOnRunning(e -> {
        //     Platform.runLater(() -> {
        //         songSearchBox.setPromptText("Searching...");
        //         songSearchBox.show();
        //     });
        // });

        searchTask.setOnSucceeded(e -> {
            List<Song> results = searchTask.getValue();
            Platform.runLater(() -> {
                searchResults.setAll(results);
                songSearchBox.setItems(searchResults);
                //songSearchBox.setPromptText(results.isEmpty() ? "No results found" : "Select a song to add");
                songSearchBox.show();
            });
        });

        searchTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                songSearchBox.setPromptText("Search failed - try again");
                searchResults.clear();
                songSearchBox.setItems(searchResults);
                System.err.println("Search failed: " + searchTask.getException().getMessage());
            });
        });

        ThreadManager.submitTask(searchTask);
    }

    private void addSongToPlaylist(Song song) {
        Task<Void> addTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                rmiClient.addSongToPlaylist(playlist.getId(), song.getId());
                return null;
            }
        };

        addTask.setOnRunning(e -> {
            songSearchBox.setDisable(true);
            songSearchBox.setPromptText("Adding song...");
        });

        addTask.setOnSucceeded(e -> {
            ThreadManager.runOnFxThread(() -> {
                songSearchBox.setDisable(false);
                songSearchBox.setPromptText("Search songs to add...");
                loadSongs();
            });
        });

        addTask.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> {
                songSearchBox.setDisable(false);
                songSearchBox.setPromptText("Failed to add - try again");
                new Alert(Alert.AlertType.ERROR, 
                    "Failed to add song: " + addTask.getException().getMessage()).show();
            });
        });

        ThreadManager.submitTask(addTask);
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
            SimpleStringProperty prop = new SimpleStringProperty("Loading...");
            Song song = cellData.getValue();
            Task<String> artistTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    Artist artist = rmiClient.getArtistById(song.getArtistId());
                    return artist != null ? artist.getName() : "Unknown Artist";
                }
            };
            artistTask.setOnSucceeded(e -> prop.set(artistTask.getValue()));
            artistTask.setOnFailed(e -> prop.set("Unknown Artist"));
            ThreadManager.submitTask(artistTask);
            return prop;
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
        Task<Set<Song>> task = new Task<>() {
            @Override
            protected Set<Song> call() throws Exception {
                // Fetch songs from the server/database here!
                return playlist.getSongs();
            }
        };

        task.setOnSucceeded(e -> {
            Set<Song> songs = task.getValue();
            songsTable.getItems().setAll(songs);
            if (infoLabel != null) {
                infoLabel.setText(songs.size() + " songs");
            }
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                if (infoLabel != null) {
                    infoLabel.setText("Failed to load songs");
                }
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to load playlist songs");
                alert.setContentText(task.getException().getMessage());
                alert.showAndWait();
            });
        });

        ThreadManager.submitTask(task);
    }

    private void handlePlayAll() {
        if (!songsTable.getItems().isEmpty()) {
            mainAppController.playSongs(new ArrayList<>(songsTable.getItems()));
        }
    }

    private void handleShuffle() {
        if (!songsTable.getItems().isEmpty()) {
            List<Song> shuffledSongs = new ArrayList<>(songsTable.getItems());
            Collections.shuffle(shuffledSongs);
            mainAppController.playSongs(shuffledSongs);
        }
    }

    private void handleSongPlay(Song song) {
        try {
            mainAppController.playSong(song);
        } catch (Exception e) {
            ThreadManager.runOnFxThread(() -> 
                UiComponent.showError("Error", "Failed to play song: " + e.getMessage())
            );
        }
    }
}