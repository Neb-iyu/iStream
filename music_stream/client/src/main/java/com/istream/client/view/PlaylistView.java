package com.istream.client.view;
import java.rmi.RemoteException;
import java.util.List;

import com.istream.model.Playlist;
import com.istream.model.Song;
import com.istream.rmi.MusicService;
import com.istream.client.service.AudioService;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class PlaylistView extends ScrollPane {
    private final MusicService musicService;
    private final int userId;
    private final VBox playlistBox;
    private final AudioService audioService;
    private final Playlist playlist;
    private List<Song> songs;
    public PlaylistView(MusicService musicService, int userId, AudioService audioService, Playlist playlist) {
        this.musicService = musicService;
        this.userId = userId;
        this.playlistBox = new VBox();
        this.audioService = audioService;
        this.playlist = playlist;
        initialize();
    }

    private void getSongs() {
        try {
            songs = musicService.getSongsByPlaylistId(playlist.getId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        createPlaylistView(songs);
    }

    private void createPlaylistView(List<Song> songs) {
        playlistBox.getChildren().add(new Label("Playlist"));
        for (Song song : songs) {
            Button playButton = new Button();
            HBox songBox = new HBox();
            songBox.getChildren().add(new ImageView(song.getCoverArtPath()));
            songBox.getChildren().add(new Label(song.getTitle()));
            songBox.getChildren().add(new Label(song.getArtist()));
            playlistBox.getChildren().add(songBox);
            playButton.getChildren().add(songBox);
            playButton.setOnAction(e -> {
                audioService.playStream(musicService.streamSong(song.getId()));
            });
            playlistBox.getChildren().add(playButton);
        }
    }

}