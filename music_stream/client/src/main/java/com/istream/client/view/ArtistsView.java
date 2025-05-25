package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.ArtistsViewController;
import com.istream.client.service.RMIClient;
import com.istream.client.controller.MainAppController;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class ArtistsView extends VBox {
    private final ArtistsViewController controller;

    public ArtistsView(RMIClient rmiClient, MainAppController mainAppController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/ArtistsView.fxml"));
        loader.setControllerFactory(param -> new ArtistsViewController(rmiClient, mainAppController));
        loader.setRoot(this);
        loader.load();
        this.controller = loader.getController();
    }
}