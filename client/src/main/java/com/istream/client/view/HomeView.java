package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.HomeViewController;
import com.istream.client.controller.MainAppController;
import com.istream.client.service.RMIClient;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class HomeView extends VBox {
    private final HomeViewController controller;

    public HomeView(RMIClient rmiClient, MainAppController mainAppController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/HomeView.fxml"));
        loader.setControllerFactory(param -> new HomeViewController(rmiClient, mainAppController));
        VBox content = loader.load();
        getChildren().add(content);
        this.controller = loader.getController();
    }
}