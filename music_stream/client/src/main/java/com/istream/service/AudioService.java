package com.istream.service;

import com.istream.controller.AuthController;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
//media player import
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AudioService {
    private MediaPlayer mediaPlayer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public void playStream(byte[] stream) {
        executor.submit(() -> {
            try {
                Media media = createMediaFromStream(stream);
                Platform.runLater(() -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.dispose();
                    }
                    mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.play();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Playback Error", e.getMessage()));
            }
        });
    }
    
    private Media createMediaFromStream(byte[] stream) throws IOException {
        // Implementation for streaming audio
        try {
            File tempFile = File.createTempFile("stream", ".mp3");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(stream);
            }
            return new Media(tempFile.toURI().toString());
        }


    }
    
    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }
    
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
    
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}