package com.istream.client.controller;

import com.istream.rmi.MusicService;
import com.istream.client.controller.MainAppController;
import com.istream.client.util.UiComponent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.concurrent.Task;

public class ArtistViewController {
    @FXML private VBox songContainer;
    @FXML private VBox albumContainer;
    @FXML private HBox songBox;
    @FXML private HBox albumBox;
    @FXML private ScrollPane songScrollPane;
    @FXML private ScrollPane albumScrollPane;
    
    private final MusicService musicService;
    private final int userId;
    private final int artistId;
    private final MainAppController mainAppController;

    public ArtistViewController(MusicService musicService, int userId, int artistId, MainAppController mainAppController) {
        this.musicService = musicService;
        this.userId = userId;
        this.artistId = artistId;
        this.mainAppController = mainAppController;
    }

    @FXML
    public void initialize() {
        UiComponent.configureScrollPane(songScrollPane);
        UiComponent.configureScrollPane(albumScrollPane);
        loadSongs();
        loadAlbums();
    }

    private void loadSongs() {
        Task<List<Song>> task = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return musicService.getSongsByArtistId(artistId);
            }
        };
        
        
    }
    
}
