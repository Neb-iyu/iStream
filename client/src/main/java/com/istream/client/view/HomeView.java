package com.istream.client.view;

import java.io.IOException;

import com.istream.client.controller.HomeViewController;
import com.istream.client.controller.MainAppController;
import com.istream.client.service.RMIClient;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class HomeView extends ScrollPane {
    private final HomeViewController controller;

    public HomeView(RMIClient rmiClient, MainAppController mainAppController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/HomeView.fxml"));
        loader.setControllerFactory(param -> new HomeViewController(rmiClient, mainAppController));
        ScrollPane content = loader.load();
        
        // Configure ScrollPane
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        // Set content
        setContent(content);
        this.controller = loader.getController();
    }
}