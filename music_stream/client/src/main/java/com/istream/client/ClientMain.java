package com.istream.client;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.istream.client.controller.AuthController;
import com.istream.client.controller.PlayerController;
import com.istream.client.controller.MainAppController;
import com.istream.client.service.RMIClient;
import com.istream.client.service.RMIClientImpl;
import com.istream.client.util.SessionHolder;
import com.istream.rmi.MusicService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class ClientMain extends Application {
    private Stage primaryStage;
    private MusicService musicService;
    private RMIClient rmiClient;
    private SessionHolder sessionHolder; 
    private MainAppController mainAppController;
    
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
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            this.musicService = (MusicService) registry.lookup("MusicService");
            this.rmiClient = new RMIClientImpl("localhost", 1099);
            this.sessionHolder = new SessionHolder(this.musicService);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAndExit("RMI Connection Failed", "Could not connect to the server: " + e.getMessage());
        }
    }
    
    private void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/LoginView.fxml"));
            Parent loginView = loader.load();
            AuthController controller = loader.getController();
            
            controller.initServices(musicService, sessionHolder);
            controller.setOnLoginSuccess(this::handleSuccessfulLogin);

            Scene scene = new Scene(loginView, 400, 300);
            
            try {
                String cssPath = "/css/auth.css";
                String cssUrl = getClass().getResource(cssPath).toExternalForm();
                scene.getStylesheets().add(cssUrl);
            } catch (NullPointerException e) {
                System.err.println("Warning: Could not load CSS file /css/auth.css");
            }
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAndExit("Load Error", "Failed to load login screen: " + e.getMessage());
        }
    }
    
    private void handleSuccessfulLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/MainAppView.fxml"));
            Parent mainAppView = loader.load();
            mainAppController = loader.getController(); // Get the controller
            mainAppController.initializeServices(musicService, rmiClient, sessionHolder); // Pass services

            Scene scene = new Scene(mainAppView, 1200, 800);
            // Optional: Load CSS for the main app view if any
            // try {
            //     String cssPath = "/css/main_app.css"; // Example CSS file
            //     String cssUrl = getClass().getResource(cssPath).toExternalForm();
            //     scene.getStylesheets().add(cssUrl);
            // } catch (NullPointerException e) {
            //     System.err.println("Warning: Could not load CSS file /css/main_app.css");
            // }
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen(); // Center the larger window
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAndExit("Application Load Error", "Failed to load main application view: " + e.getMessage());
        }
    }

    private void showErrorAndExit(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

