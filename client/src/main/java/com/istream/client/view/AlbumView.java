package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.AlbumViewController;
import com.istream.client.controller.MainAppController;
import com.istream.client.service.RMIClient;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;

public class AlbumView extends VBox {
    private final AlbumViewController controller;

    public AlbumView(RMIClient rmiClient, int albumId, MainAppController mainAppController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/AlbumView.fxml"));
        loader.setControllerFactory(param -> new AlbumViewController(rmiClient, albumId, mainAppController));
        Parent content = loader.load();
        getChildren().add(content);
        this.controller = loader.getController();
    }
}
