package com.istream.client.view;

import java.io.IOException;
import com.istream.client.controller.LikedViewController;
import com.istream.rmi.MusicService;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class LikedView extends VBox {
    private final LikedViewController controller;

    public LikedView(MusicService musicService, int userId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/LikedPage.fxml"));
        this.controller = new LikedViewController(musicService, userId);
        loader.setController(controller);
        loader.setRoot(this);
        loader.load();
    }
} 