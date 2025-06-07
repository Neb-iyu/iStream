package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.LikedViewController;
import com.istream.client.controller.MainAppController;
import com.istream.client.service.RMIClient;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class LikedView extends VBox {
    private final LikedViewController controller;

    public LikedView(RMIClient rmiClient, MainAppController mainAppController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/LikedView.fxml"));
        loader.setControllerFactory(param -> new LikedViewController(rmiClient, mainAppController));
        VBox content = loader.load();
        getChildren().add(content);
        this.controller = loader.getController();
    }
}