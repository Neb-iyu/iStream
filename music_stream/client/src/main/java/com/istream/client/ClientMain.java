package com.istream.client;

import java.io.IOException;

import com.istream.client.controller.AuthController;
import com.istream.client.controller.MainAppController;
import com.istream.client.service.RMIClient;
import com.istream.client.service.RMIClientImpl;
import com.istream.client.util.SessionHolder;

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
    private static ClientMain instance;
    private Stage primaryStage;
    private RMIClient rmiClient;
    private SessionHolder sessionHolder; 
    private MainAppController mainAppController;
    
    @Override
    public void start(Stage primaryStage) {
        instance = this;
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
            this.rmiClient = new RMIClientImpl("localhost", 1099);
            this.sessionHolder = new SessionHolder(this.rmiClient);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAndExit("RMI Connection Failed", "Could not connect to the server: " + e.getMessage());
        }
    }
    
    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/content/LoginView.fxml"));
            Parent root = loader.load();
            AuthController controller = loader.getController();
            controller.initServices(rmiClient, sessionHolder);
            controller.setOnLoginSuccess(this::showMainApp);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/istream/styles/main.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAndExit("Error", "Could not load login screen: " + e.getMessage());
        }
    }
    
    private void showMainApp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/istream/fxml/MainAppView.fxml"));
            Parent root = loader.load();
            mainAppController = loader.getController();
            mainAppController.initializeServices(rmiClient, sessionHolder);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/istream/styles/main.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAndExit("Error", "Could not load main application: " + e.getMessage());
        }
    }
    
    private void showErrorAndExit(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }
    
    public static ClientMain getInstance() {
        return instance;
    }
    
    public static void main(String[] args) {
        launch();
    }
}

