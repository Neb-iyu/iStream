package com.istream.client.controller;

//package com.musicstreamer.client.controller;

import java.rmi.RemoteException;

import com.istream.client.util.SessionHolder;
import com.istream.rmi.MusicService;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class AuthController {
    @FXML private AnchorPane root;
    @FXML private Pane logoPane;
    @FXML private Pane formPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label emailLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;
    @FXML private Label switchViewLabel;
    @FXML private Button switchViewButton;

    private MusicService musicService;
    private SessionHolder sessionManager;
    private Runnable onLoginSuccess;
    private boolean isLoginView = true;

    public AuthController() {}

    public void initServices(MusicService musicService, SessionHolder sessionManager) {
        this.musicService = musicService;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void initialize() {
        setupFormValidation();
        updateViewState();
    }

    private void setupFormValidation() {
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        if (emailField != null) {
            emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        }
    }

    private void validateForm() {
        boolean valid = !usernameField.getText().isEmpty() && !passwordField.getText().isEmpty();
        if (!isLoginView) {
            valid = valid && !emailField.getText().isEmpty();
        }
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
        String email = emailField.getText();

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
                switchToLoginView();
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

    @FXML
    private void handleSwitchView() {
        if (isLoginView) {
            switchToRegisterView();
        } else {
            switchToLoginView();
        }
    }

    private void switchToLoginView() {
        isLoginView = true;
        updateViewState();
    }

    private void switchToRegisterView() {
        isLoginView = false;
        updateViewState();
    }

    private void updateViewState() {
        if (isLoginView) {
            if (emailField != null) emailField.setVisible(false);
            if (emailLabel != null) emailLabel.setVisible(false);
            loginButton.setVisible(true);
            registerButton.setVisible(false);
            switchViewLabel.setText("Don't have an account?");
            switchViewButton.setText("Register");
        } else {
            if (emailField != null) emailField.setVisible(true);
            if (emailLabel != null) emailLabel.setVisible(true);
            loginButton.setVisible(false);
            registerButton.setVisible(true);
            switchViewLabel.setText("Already have an account?");
            switchViewButton.setText("Login");
        }
        validateForm();
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

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public static Parent createLoginView(MusicService musicService, SessionHolder sessionManager) {
        try {
            FXMLLoader loader = new FXMLLoader(AuthController.class.getResource("/com/istream/fxml/content/LoginView.fxml"));
            loader.setControllerFactory(param -> new AuthController(musicService, sessionManager));
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load LoginView.fxml", e);
        }
    }

    public static Parent createRegisterView(MusicService musicService, SessionHolder sessionManager) {
        try {
            FXMLLoader loader = new FXMLLoader(AuthController.class.getResource("/com/istream/fxml/content/RegisterView.fxml"));
            loader.setControllerFactory(param -> new AuthController(musicService, sessionManager));
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RegisterView.fxml", e);
        }
    }
}
