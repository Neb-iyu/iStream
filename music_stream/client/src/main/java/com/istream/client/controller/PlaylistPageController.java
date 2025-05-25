package com.istream.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import com.istream.model.Playlist;
import com.istream.model.Song;
import com.istream.client.service.RMIClient;
import java.util.List;

public class PlaylistPageController {
    @FXML private ImageView playlistCover;
    @FXML private Label playlistTitle;
    @FXML private Label playlistInfo;
    @FXML private Button playButton;
    @FXML private Button shuffleButton;
    @FXML private TableView<Song> songsTable;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> albumColumn;
    @FXML private TableColumn<Song, Integer> durationColumn;

    private RMIClient rmiClient;
    private Playlist currentPlaylist;
    private ObservableList<Song> songs;

    public void initialize() {
        setupTableColumns();
        setupEventHandlers();
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
    }

    private void setupEventHandlers() {
        songsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song selectedSong = songsTable.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    handleSongPlay(selectedSong);
                }
            }
        });
    }

    public void setRMIClient(RMIClient rmiClient) {
        this.rmiClient = rmiClient;
    }

    public void loadPlaylist(Playlist playlist) {
        this.currentPlaylist = playlist;
        updateUI();
        loadSongs();
    }

    private void updateUI() {
        playlistTitle.setText(currentPlaylist.getName());
        playlistInfo.setText(currentPlaylist.getSongs().size() + " songs");
        
        // Set default playlist cover if no custom cover is available
        playlistCover.setImage(new Image(getClass().getResourceAsStream("/com/istream/images/default-playlist.png")));
    }

    private void loadSongs() {
        try {
            // Get songs from the playlist's HashSet
            songs = FXCollections.observableArrayList(currentPlaylist.getSongs());
            songsTable.setItems(songs);
        } catch (Exception e) {
            e.printStackTrace();
            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load playlist songs");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handlePlayAll() {
        if (!songs.isEmpty()) {
            handleSongPlay(songs.get(0));
        }
    }

    @FXML
    private void handleShuffle() {
        if (!songs.isEmpty()) {
            int randomIndex = (int) (Math.random() * songs.size());
            handleSongPlay(songs.get(randomIndex));
        }
    }

    private void handleSongPlay(Song song) {
        try {
            rmiClient.recordPlay(song.getId());
            // TODO: Implement actual song playback
        } catch (Exception e) {
            e.printStackTrace();
            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to play song");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public static Parent createView() {
        try {
            FXMLLoader loader = new FXMLLoader(PlaylistPageController.class.getResource("/com/istream/fxml/content/PlaylistPage.fxml"));
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load PlaylistPage.fxml", e);
        }
    }
} 