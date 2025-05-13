package com.istream.controller;

import com.istream.service.RMIClient;
import com.istream.model.Song;
import com.istream.service.AudioService;
import com.istream.view.SongListView;

import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;





public class PlayerController {
    private final RMIClient rmiClient;
    private final AudioService audioService;
    private final ObservableList<Song> songList = FXCollections.observableArrayList();
    
    public PlayerController(RMIClient rmiClient) {
        this.rmiClient = rmiClient;
        this.audioService = new AudioService();
        initialize();
    }
    
    private void initialize() {
        loadSongs();
        setupEventHandlers();
    }
    
    private void loadSongs() {
        Task<List<Song>> loadTask = new Task<>() {
            @Override
            protected List<Song> call() throws Exception {
                return rmiClient.getAllSongs();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            songList.setAll(loadTask.getValue());
        });
        
        new Thread(loadTask).start();
    }
    
    private void setupEventHandlers() {
        // Setup UI event bindings
    }
    
    public Parent getView() {
        VBox root = new VBox(10);
        root.getStyleClass().add("player-view");
        
        // Create song list view
        SongListView songListView = new SongListView(songList, song -> {
            try {
                byte[] songData = rmiClient.streamSong(song.getId());
                audioService.playStream(songData);
            } catch (Exception e) {
                // Handle error
                e.printStackTrace();
            }
        });
        
        // Create player controls
        HBox controls = new HBox(10);
        controls.getStyleClass().add("player-controls");
        
        Button playButton = new Button("▶");
        Button pauseButton = new Button("⏸");
        Button stopButton = new Button("⏹");
        
        playButton.setOnAction(e -> audioService.play());
        pauseButton.setOnAction(e -> audioService.pause());
        stopButton.setOnAction(e -> audioService.stop());
        
        controls.getChildren().addAll(playButton, pauseButton, stopButton);
        
        root.getChildren().addAll(songListView, controls);
        return root;
    }
}