package com.istream.client.view;

import java.io.IOException;
import com.istream.client.controller.ArtistsViewController;
import com.istream.rmi.MusicService;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class ArtistsView extends VBox {
    private final ArtistsViewController controller;

    public ArtistsView(MusicService musicService, int userId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/ArtistsPage.fxml"));
        this.controller = new ArtistsViewController(musicService, userId);
        loader.setController(controller);
        loader.setRoot(this);
        loader.load();
    }
} 