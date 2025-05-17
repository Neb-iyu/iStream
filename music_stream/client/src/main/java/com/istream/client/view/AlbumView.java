package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.AlbumViewController;
import com.istream.rmi.MusicService;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import com.istream.client.controller.MainAppController;

public class AlbumView extends VBox {
    private final AlbumViewController controller;

    public AlbumView(MusicService musicService, int userId, MainAppController mainAppController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/AlbumPage.fxml"));
        this.controller = new AlbumViewController(musicService, userId, mainAppController);
        loader.setController(controller);
        loader.setRoot(this);
        loader.load();
    }
}
