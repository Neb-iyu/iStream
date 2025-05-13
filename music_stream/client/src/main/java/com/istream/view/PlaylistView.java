package com.istream.view;
import java.rmi.RemoteException;
import java.util.List;

import com.istream.model.Playlist;
import com.istream.rmi.MusicService;

import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PlaylistView extends VBox {
    private final MusicService musicService;
    private final int userId;
    
    public PlaylistView(MusicService musicService, int userId) {
        this.musicService = musicService;
        this.userId = userId;
        initialize();
    }
    
    private void initialize() {
        // Playlist creation controls
        TextField playlistNameField = new TextField();
        Button createButton = new Button("Create Playlist");
        createButton.setOnAction(e -> createPlaylist(playlistNameField.getText()));
        
        // Current playlists list
        ListView<Playlist> playlistList = new ListView<>();
        loadPlaylists(playlistList);
        
        getChildren().addAll(
            new HBox(playlistNameField, createButton),
            playlistList
        );
    }
    
    private void createPlaylist(String name) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws RemoteException {
                musicService.createPlaylist(userId, name);
                return null;
            }
        };
        new Thread(task).start();
    }
    
    private void loadPlaylists(ListView<Playlist> listView) {
        Task<List<Playlist>> task = new Task<>() {
            @Override
            protected List<Playlist> call() throws RemoteException {
                return musicService.getUserPlaylists(userId);
            }
        };
        
        task.setOnSucceeded(e -> 
            listView.getItems().setAll(task.getValue())
        );
        
        new Thread(task).start();
    }
}