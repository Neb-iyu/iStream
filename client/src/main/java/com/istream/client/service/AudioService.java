package com.istream.client.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.istream.model.Song;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioService {
    private MediaPlayer mediaPlayer;
    private LinkedList<Song> songQueue;
    private Queue<byte[]> audioDataQueue;
    private Song currentSong;
    private boolean isPlaying;
    private Duration currentPosition;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinkedList<Song> history;
    private Stack<byte[]> audioDataHistory;
    
    public AudioService() {
        this.songQueue = new LinkedList<>();
        this.audioDataQueue = new LinkedList<>();
        this.history = new LinkedList<>();
        this.isPlaying = false;
        this.audioDataHistory = new Stack<>();
    }
    
    public void playSong(Song song, byte[] audioData) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        if (currentSong != null) {
            history.addFirst(currentSong);
            audioDataHistory.push(audioData);
        }
        currentSong = song;
        Media media = createMediaFromStream(audioData);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia(this::playNextInQueue);
        mediaPlayer.play();
        isPlaying = true;
    }
    
    public CompletableFuture<Void> addToQueueAsync(Song song, byte[] audioData) {
        return CompletableFuture.runAsync(() -> {
            synchronized (songQueue) {
                songQueue.add(song);
                audioDataQueue.add(audioData);
            }
        }, executor);
    }
    
    public void playNext() {
        if (!songQueue.isEmpty() && !audioDataQueue.isEmpty()) {
            Song nextSong;
            byte[] nextAudioData;
            synchronized (songQueue) {
                nextSong = songQueue.poll();
                nextAudioData = audioDataQueue.poll();
            }
            playSong(nextSong, nextAudioData);
        }
    }
    
    public void playPrevious() {
        if (!history.isEmpty()) {
            Song previousSong = history.removeFirst();
            if (currentSong != null) {
                songQueue.addFirst(currentSong);
            }
            try {
                byte[] audioData = getAudioDataForSong(previousSong);
                playSong(previousSong, audioData);
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to play previous song: " + e.getMessage()));
            }
        }
    }
    
    private byte[] getAudioDataForSong(Song song) {
        return audioDataHistory.isEmpty() ? null : audioDataHistory.pop();
    }
    
    public void pause() {
        if (mediaPlayer != null) {
            currentPosition = mediaPlayer.getCurrentTime();
            mediaPlayer.pause();
            isPlaying = false;
        }
    }
    
    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(currentPosition);
            mediaPlayer.play();
            isPlaying = true;
        }
    }
    
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }
    
    public Song getCurrentSong() {
        return currentSong;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public Duration getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentTime() : Duration.ZERO;
    }
    
    public Duration getTotalDuration() {
        return mediaPlayer != null ? mediaPlayer.getTotalDuration() : Duration.ZERO;
    }
    
    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }
    
    public void seek(Duration position) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(position);
        }
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
    
    private Media createMediaFromStream(byte[] stream) {
        try {
            File tempFile = File.createTempFile("stream", ".mp3");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(stream);
            }
            return new Media(tempFile.toURI().toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create media from stream: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    public void clearQueue() {
        synchronized (songQueue) {
            songQueue.clear();
            audioDataQueue.clear();
        }
    }
    
    public boolean isQueueEmpty() {
        synchronized (songQueue) {
            return songQueue.isEmpty() && audioDataQueue.isEmpty();
        }
    }
    
    private void playNextInQueue() {
        if (!songQueue.isEmpty() && !audioDataQueue.isEmpty()) {
            Song nextSong;
            byte[] nextAudioData;
            synchronized (songQueue) {
                nextSong = songQueue.poll();
                nextAudioData = audioDataQueue.poll();
            }
            playSong(nextSong, nextAudioData);
        } else {
            isPlaying = false;
        }
    }
    
    public void skipToNext() {
        if (!songQueue.isEmpty()) {
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
    
    public Queue<Song> getSongQueue() {
        synchronized (songQueue) {
            return new LinkedList<>(songQueue);
        }
    }
}