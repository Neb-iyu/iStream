package com.istream.client.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioService {
    private MediaPlayer mediaPlayer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Queue<byte[]> audioQueue;
    private boolean isPlaying;
    
    public AudioService() {
        this.audioQueue = new LinkedList<>();
        this.isPlaying = false;
    }
    
    public void playStream(byte[] stream) {
        executor.submit(() -> {
            try {
                Media media = createMediaFromStream(stream);
                Platform.runLater(() -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.dispose();
                    }
                    mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.setOnEndOfMedia(this::playNextInQueue);
                    mediaPlayer.play();
                    isPlaying = true;
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
        } catch (IOException e) {
            throw new IOException("Failed to create media from stream: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public void addToQueue(byte[] audioStream) {
        audioQueue.offer(audioStream);
        if (!isPlaying) {
            playNextInQueue();
        }
    }
    
    public void clearQueue() {
        audioQueue.clear();
    }
    
    public boolean isQueueEmpty() {
        return audioQueue.isEmpty();
    }
    
    private void playNextInQueue() {
        byte[] nextStream = audioQueue.poll();
        if (nextStream != null) {
            playStream(nextStream);
        } else {
            isPlaying = false;
        }
    }
    
    public void skipToNext() {
        if (!audioQueue.isEmpty()) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            playNextInQueue();
        }
    }
    
    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying = true;
        }
    }
    
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }
    
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }
}