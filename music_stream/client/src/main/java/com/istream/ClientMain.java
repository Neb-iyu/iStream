package com.istream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.istream.rmi.MusicService;
import com.istream.service.RMIClient;
import com.istream.view.LoginView;
import com.istream.controller.AuthController;
import com.istream.controller.PlayerController;

/**
 * JavaFX App
 */
public class ClientMain extends Application {
    private Stage primaryStage;
    private RMIClient rmiClient;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeRMI();
        showLoginScreen();
        
        primaryStage.setTitle("iStream");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }
    
    private void initializeRMI() {
        try {
            this.rmiClient = new RMIClient("localhost", 1099);
        } catch (Exception e) {
            showErrorAndExit("RMI Connection Failed", e.getMessage());
        }
    }
    
    private void showLoginScreen() {
        AuthController authController = new AuthController(rmiClient, this::handleSuccessfulLogin);
        Scene scene = new Scene(authController.getView(), 400, 300);
        scene.getStylesheets().add(getClass().getResource("/css/auth.css").toExternalForm());
        primaryStage.setScene(scene);
    }
    
    private void handleSuccessfulLogin() {
        PlayerController playerController = new PlayerController(rmiClient);
        Scene scene = new Scene(playerController.getView(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        primaryStage.setScene(scene);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
    
    // static void setRoot(String fxml) throws IOException {
    //     scene.setRoot(loadFXML(fxml));
    // }

    // private static Parent loadFXML(String fxml) throws IOException {
    //     FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    //     return fxmlLoader.load();
    // }

