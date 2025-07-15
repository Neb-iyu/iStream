package com.istream.client.controller;

import java.rmi.RemoteException;
import java.util.regex.Pattern;

import com.istream.client.service.RMIClient;
import com.istream.client.util.SessionHolder;

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

    private RMIClient rmiClient;
    private SessionHolder sessionManager;
    private Runnable onLoginSuccess;
    private boolean isLoginView = true;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MIN_USERNAME_LENGTH = 3;

    public AuthController() {}

    public AuthController(RMIClient rmiClient, SessionHolder sessionManager) {
        this.rmiClient = rmiClient;
        this.sessionManager = sessionManager;
    }

    public AuthController(RMIClient rmiClient, SessionHolder sessionManager, boolean isLoginView) {
        this.rmiClient = rmiClient;
        this.sessionManager = sessionManager;
        this.isLoginView = isLoginView;
    }

    public void initServices(RMIClient rmiClient, SessionHolder sessionManager) {
        this.rmiClient = rmiClient;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void initialize() {
        setupFormValidation();
    }

    private void setupFormValidation() {
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        if (emailField != null) {
            emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        }
    }

    private String validateUsername(String username) {
        if (username.isEmpty()) {
            return "Username is required";
        }
        if (username.length() < MIN_USERNAME_LENGTH) {
            return "Username must be at least " + MIN_USERNAME_LENGTH + " characters";
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Username can only contain letters, numbers, and underscores";
        }
        return null;
    }

    private String validatePassword(String password) {
        if (password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters";
        }
        return null;
    }

    private String validateEmail(String email) {
        if (email.isEmpty()) {
            return "Email is required";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Please enter a valid email address";
        }
        return null;
    }

    private void validateForm() {
        String usernameError = validateUsername(usernameField.getText());
        String passwordError = validatePassword(passwordField.getText());
        String emailError = null;
        
        if (!isLoginView && emailField != null) {
            emailError = validateEmail(emailField.getText());
        }

        StringBuilder errorMessage = new StringBuilder();
        if (usernameError != null) {
            errorMessage.append(usernameError).append("\n");
        }
        if (passwordError != null) {
            errorMessage.append(passwordError).append("\n");
        }
        if (emailError != null) {
            errorMessage.append(emailError).append("\n");
        }

        boolean isValid = usernameError == null && passwordError == null && 
                         (isLoginView || emailError == null);

        if (loginButton != null) loginButton.setDisable(!isValid);
        if (registerButton != null) registerButton.setDisable(!isValid);

        if (!isValid) {
            showError(errorMessage.toString().trim());
        } else {
            statusLabel.setStyle("-fx-text-fill: #d9cccc");
            statusLabel.setText("");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    System.out.println("Attempting to connect to server...");
                    String sessionToken = rmiClient.login(username, password);
                    System.out.println("Server response received: " + (sessionToken != null ? "Success" : "Failed"));
                    
                    if (sessionToken != null) {
                        sessionManager.createSession(sessionToken, username);
                        return true;
                    }
                    return false;
                } catch (RemoteException e) {
                    System.err.println("Remote Exception during login: " + e.getMessage());
                    e.printStackTrace();
                    throw new Exception("Server connection failed: " + e.getMessage(), e);
                } catch (Exception e) {
                    System.err.println("Unexpected error during login: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        loginTask.setOnSucceeded(e -> {
            if (loginTask.getValue()) {
                onLoginSuccess.run();
            } else {
                showError("Invalid username or password");
            }
        });

        loginTask.setOnFailed(e -> {
            String errorMessage = "Login failed: ";
            if (loginTask.getException() != null) {
                errorMessage += loginTask.getException().getMessage();
                System.err.println("Login error details: " + loginTask.getException().getMessage());
                loginTask.getException().printStackTrace();
            }
            showError(errorMessage);
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
                    System.out.println("Attempting to register user...");
                    boolean result = rmiClient.register(username, password, email);
                    System.out.println("Registration result: " + result);
                    return result;
                } catch (RemoteException e) {
                    System.err.println("Remote Exception during registration: " + e.getMessage());
                    e.printStackTrace();
                    throw new Exception("Server connection failed: " + e.getMessage(), e);
                } catch (Exception e) {
                    System.err.println("Unexpected error during registration: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
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
            String errorMessage = "Registration error: ";
            if (registerTask.getException() != null) {
                errorMessage += registerTask.getException().getMessage();
                System.err.println("Registration error details: " + registerTask.getException().getMessage());
                registerTask.getException().printStackTrace();
            }
            showError(errorMessage);
        });

        bindTaskToUI(registerTask);
        new Thread(registerTask).start();
    }

    @FXML
    private void handleSwitchView() {
        if (isLoginView) {
            System.out.println("Switching to register view");
            switchToRegisterView();
        } else {
            System.out.println("Switching to login view");
            switchToLoginView();
        }
    }

    private void switchToLoginView() {
        try {
            Parent loginView = createLoginView(rmiClient, sessionManager, onLoginSuccess);
            root.getChildren().clear();
            root.getChildren().add(loginView);
        isLoginView = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchToRegisterView() {
        try {
            Parent registerView = createRegisterView(rmiClient, sessionManager, onLoginSuccess);
            root.getChildren().clear();
            root.getChildren().add(registerView);
            System.out.println("Switched to register view");
        isLoginView = false;
        System.out.println("isLoginView set to " + isLoginView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindTaskToUI(Task<?> task) {
        if (loginButton != null) loginButton.disableProperty().bind(task.runningProperty());
        if (registerButton != null) registerButton.disableProperty().bind(task.runningProperty());
        if (statusLabel != null) {
            statusLabel.textProperty().unbind();
        statusLabel.textProperty().bind(task.messageProperty());
        }
    }

    private void showError(String message) {
        if (statusLabel != null) {
            statusLabel.textProperty().unbind();
            statusLabel.setStyle("-fx-text-fill: #e74c3c");
            statusLabel.setText(message);
            System.err.println("Error displayed to user: " + message);
        }
    }

    private void showSuccess(String message) {
        if (statusLabel != null) {
            statusLabel.textProperty().unbind();
        statusLabel.setStyle("-fx-text-fill: #2ecc71");
        statusLabel.setText(message);
        }
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public static Parent createLoginView(RMIClient rmiClient, SessionHolder sessionManager, Runnable onLoginSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(AuthController.class.getResource("/com/istream/fxml/content/LoginView.fxml"));
            loader.setControllerFactory(param -> {
                AuthController ctrl = new AuthController(rmiClient, sessionManager, true);
                ctrl.setOnLoginSuccess(onLoginSuccess);
                return ctrl;
            });
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load LoginView.fxml", e);
        }
    }

    public static Parent createRegisterView(RMIClient rmiClient, SessionHolder sessionManager, Runnable onLoginSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(AuthController.class.getResource("/com/istream/fxml/content/RegisterView.fxml"));
            loader.setControllerFactory(param -> {
                AuthController ctrl = new AuthController(rmiClient, sessionManager, false);
                ctrl.setOnLoginSuccess(onLoginSuccess);
                return ctrl;
            });
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RegisterView.fxml", e);
        }
    }
}
