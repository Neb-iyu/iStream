package com.istream.client.controller;

import java.util.List;

import com.istream.client.service.AudioService;
import com.istream.client.service.RMIClient;
import com.istream.client.util.ThreadManager;
import com.istream.client.util.UiComponent;
import com.istream.client.view.SongListView;
import com.istream.model.Song;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class PlayerController {
    private final RMIClient rmiClient;
    private final AudioService audioService;
    private final ObservableList<Song> songList = FXCollections.observableArrayList();
    
    @FXML private VBox root;
    @FXML private ListView<Song> songListView;
    
    public PlayerController(RMIClient rmiClient) {
        this.rmiClient = rmiClient;
        this.audioService = new AudioService();
    }
    
    @FXML
    public void initialize() {
        loadSongs();
        setupSongListView();
    }
    
    private void loadSongs() {
        Task<List<Song>> loadTask = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return rmiClient.getAllSongs();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            ThreadManager.runOnFxThread(() -> songList.setAll(loadTask.getValue()));
        });
        
        loadTask.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> 
                UiComponent.showError("Error", "Failed to load songs: " + loadTask.getException().getMessage())
            );
        });
        
        ThreadManager.submitTask(loadTask);
    }
    
    private void setupSongListView() {
        songListView.setItems(songList);
        songListView.setCellFactory(lv -> new SongListView.SongListCell(song -> {
            try {
                byte[] songData = rmiClient.streamSong(song.getId());
                audioService.playStream(songData);
            } catch (Exception e) {
                ThreadManager.runOnFxThread(() -> 
                    UiComponent.showError("Error", "Failed to play song: " + e.getMessage())
                );
            }
        }));
    }
    
    @FXML
    private void handlePlay() {
        audioService.play();
    }
    
    @FXML
    private void handlePause() {
        audioService.pause();
    }
    
    @FXML
    private void handleStop() {
        audioService.stop();
    }
    
    public static Parent createView(RMIClient rmiClient) {
        try {
            FXMLLoader loader = new FXMLLoader(PlayerController.class.getResource("/com/istream/fxml/content/PlayerView.fxml"));
            loader.setControllerFactory(param -> new PlayerController(rmiClient));
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load PlayerView.fxml", e);
        }
    }
}