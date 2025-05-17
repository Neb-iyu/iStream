package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.ArtistViewController;
import com.istream.rmi.MusicService;
import com.istream.client.controller.MainAppController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class ArtistView extends VBox {
    private final ArtistViewController controller;

    public ArtistView(MusicService musicService, int userId, int artistId, MainAppController mainAppController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/ArtistPage.fxml"));
        this.controller = new ArtistViewController(musicService, userId, artistId, mainAppController);
        loader.setController(controller);
        loader.setRoot(this);
        loader.load();
    }
}
