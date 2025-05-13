package com.istream.controller;

//package com.musicstreamer.client.controller;

import java.rmi.RemoteException;

import com.istream.rmi.MusicService;
import com.istream.util.SessionHolder;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField; // Assuming you have an email field for registration
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;
    @FXML private VBox authContainer;

    private final MusicService musicService;
    private final SessionHolder sessionManager;
    private Runnable onLoginSuccess;

    public AuthController(MusicService musicService, SessionHolder sessionManager) {
        this.musicService = musicService;
        this.sessionManager = sessionManager;
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    @FXML
    private void initialize() {
        setupFormValidation();
    }

    private void setupFormValidation() {
        // Real-time validation
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> 
            validateForm()
        );
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> 
            validateForm()
        );
    }

    private void validateForm() {
        boolean valid = !usernameField.getText().isEmpty() 
                     && !passwordField.getText().isEmpty();
        loginButton.setDisable(!valid);
        registerButton.setDisable(!valid);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    String sessionToken = musicService.login(username, password);
                    if (sessionToken != null) {
                        sessionManager.createSession(sessionToken, username);
                        return true;
                    }
                    return false;
                } catch (RemoteException e) {
                    throw new Exception("Server connection failed", e);
                }
            }
        };

        loginTask.setOnSucceeded(e -> {
            if (loginTask.getValue()) {
                onLoginSuccess.run();
            } else {
                showError("Invalid credentials");
            }
        });

        loginTask.setOnFailed(e -> {
            showError("Login failed: " + loginTask.getException().getMessage());
        });

        bindTaskToUI(loginTask);
        new Thread(loginTask).start();
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText(); // Get email from a new TextField or similar input

        Task<Boolean> registerTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    return musicService.register(username, password, email);
                } catch (RemoteException e) {
                    throw new Exception("Registration failed", e);
                }
            }
        };

        registerTask.setOnSucceeded(e -> {
            if (registerTask.getValue()) {
                showSuccess("Registration successful! Please login");
            } else {
                showError("Username already exists");
            }
        });

        registerTask.setOnFailed(e -> {
            showError("Registration error: " + registerTask.getException().getMessage());
        });

        bindTaskToUI(registerTask);
        new Thread(registerTask).start();
    }

    private void bindTaskToUI(Task<?> task) {
        loginButton.disableProperty().bind(task.runningProperty());
        registerButton.disableProperty().bind(task.runningProperty());
        statusLabel.textProperty().bind(task.messageProperty());
    }

    private void showError(String message) {
        statusLabel.setStyle("-fx-text-fill: #e74c3c");
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.setStyle("-fx-text-fill: #2ecc71");
        statusLabel.setText(message);
    }
}
