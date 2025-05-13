package com.istream.view;

import java.util.function.Consumer;

import com.istream.model.Song;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;


public class SongListView extends ListView<Song> {
    public SongListView(ObservableList<Song> songs, Consumer<Song> onPlay) {
        super(songs);
        setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setGraphic(null);
                } else {
                    SongTile tile = new SongTile(song);
                    tile.setOnPlayClick(() -> onPlay.accept(song));
                    setGraphic(tile);
                }
            }
        });
    }
}