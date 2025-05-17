package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.HomeViewController;
import com.istream.rmi.MusicService;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class HomeView extends VBox {
    private final HomeViewController controller;

    public HomeView(MusicService musicService, int userId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/HomePage.fxml"));
        this.controller = new HomeViewController(musicService, userId);
        loader.setController(controller);
        loader.setRoot(this);
        loader.load();
    }
}