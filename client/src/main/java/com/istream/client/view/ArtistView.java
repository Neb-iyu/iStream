package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.ArtistViewController;
import com.istream.client.service.RMIClient;
import com.istream.client.controller.MainAppController;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class ArtistView extends VBox {
    private final ArtistViewController controller;

    public ArtistView(RMIClient rmiClient, int artistId, MainAppController mainAppController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/ArtistView.fxml"));
        loader.setControllerFactory(param -> new ArtistViewController(rmiClient, artistId, mainAppController));
        loader.setRoot(this);
        loader.load();
        this.controller = loader.getController();
    }
}
