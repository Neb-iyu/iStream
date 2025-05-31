package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.AlbumViewController;
import com.istream.client.service.RMIClient;
import com.istream.client.controller.MainAppController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class AlbumView extends VBox {
    private final AlbumViewController controller;

    public AlbumView(RMIClient rmiClient, int albumId, MainAppController mainAppController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/AlbumView.fxml"));
        loader.setControllerFactory(param -> new AlbumViewController(rmiClient, albumId, mainAppController));
        loader.setRoot(this);
        loader.load();
        this.controller = loader.getController();
    }
}
