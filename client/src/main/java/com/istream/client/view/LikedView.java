package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.LikedViewController;
import com.istream.client.service.RMIClient;
import com.istream.client.controller.MainAppController;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class LikedView extends VBox {
    private final LikedViewController controller;

    public LikedView(RMIClient rmiClient, MainAppController mainAppController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/LikedView.fxml"));
        loader.setControllerFactory(param -> new LikedViewController(rmiClient, mainAppController));
        loader.setRoot(this);
        loader.load();
        this.controller = loader.getController();
    }
}