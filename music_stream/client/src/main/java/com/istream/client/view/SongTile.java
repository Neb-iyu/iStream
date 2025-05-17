package com.istream.client.view;

import com.istream.model.Song;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SongTile extends HBox {
    private final Song song;
    private Runnable onPlayClick;

    public SongTile(Song song) {
        this.song = song;
        initialize();
    }

    private void initialize() {
        // Create labels for song information
        Label titleLabel = new Label(song.getTitle());
        titleLabel.getStyleClass().add("song-title");
        
        Label artistLabel = new Label(song.getArtist());
        artistLabel.getStyleClass().add("song-artist");
        
        // Create play button
        Button playButton = new Button("▶");
        playButton.getStyleClass().add("play-button");
        playButton.setOnAction(e -> {
            if (onPlayClick != null) {
                onPlayClick.run();
            }
        });

        // Create VBox for song details
        VBox detailsBox = new VBox(titleLabel, artistLabel);
        detailsBox.getStyleClass().add("song-details");
        
        // Add components to HBox
        getChildren().addAll(detailsBox, playButton);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);
        
        // Add styling
        getStyleClass().add("song-tile");
        setSpacing(10);
    }

    public void setOnPlayClick(Runnable onPlayClick) {
        this.onPlayClick = onPlayClick;
    }
} 