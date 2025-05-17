package com.istream.client.controller;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.istream.client.service.FileUploadService;
import com.istream.client.service.RMIClient;
import com.istream.model.Song;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class UploadController {
    @FXML private Button selectFileButton;
    @FXML private Button uploadButton;
    @FXML private TextField titleField;
    @FXML private TextField artistField;
    @FXML private TextField albumField;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label selectedFileLabel;

    private final RMIClient rmiClient;
    private final FileUploadService fileUploadService;
    private File selectedFile;

    public UploadController(RMIClient rmiClient) {
        this.rmiClient = rmiClient;
        this.fileUploadService = new FileUploadService();
    }

    @FXML
    public void initialize() {
        uploadButton.setDisable(true);
        progressBar.setVisible(false);
        statusLabel.setText("");
    }

    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Audio File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.ogg", "*.m4a", "*.aac")
        );

        File file = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());
        if (file != null) {
            FileUploadService.FileValidationResult validation = fileUploadService.validateFile(file);
            if (validation.isValid()) {
                selectedFile = file;
                selectedFileLabel.setText(file.getName());
                uploadButton.setDisable(false);
                statusLabel.setText("");
            } else {
                statusLabel.setText(validation.getErrorMessage());
                uploadButton.setDisable(true);
            }
        }
    }

    @FXML
    private void handleUpload() {
        if (selectedFile == null || titleField.getText().isEmpty() || artistField.getText().isEmpty()) {
            statusLabel.setText("Please fill in all required fields");
            return;
        }

        uploadButton.setDisable(true);
        selectFileButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(0);

        CompletableFuture.runAsync(() -> {
            try {
                byte[] fileData = fileUploadService.readFile(selectedFile);
                Song song = new Song(0, titleField.getText(), artistField.getText(), albumField.getText(), "", 0, "", "", 0);
                rmiClient.uploadSong(
                    song,
                    fileData
                );
                
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Upload successful!");
                    progressBar.setProgress(1);
                    resetForm();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Upload failed: " + e.getMessage());
                    progressBar.setVisible(false);
                    uploadButton.setDisable(false);
                    selectFileButton.setDisable(false);
                });
            }
        });
    }

    private void resetForm() {
        selectedFile = null;
        selectedFileLabel.setText("");
        titleField.clear();
        artistField.clear();
        albumField.clear();
        uploadButton.setDisable(true);
        selectFileButton.setDisable(false);
        progressBar.setVisible(false);
    }
} 