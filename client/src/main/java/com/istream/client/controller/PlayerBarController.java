package com.istream.client.controller;

import com.istream.client.service.AudioService;
import com.istream.client.service.RMIClient;
import com.istream.client.util.SessionHolder;
import com.istream.client.util.ThreadManager;
import com.istream.client.util.UiComponent;
import com.istream.model.Song;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PlayerBarController {
    @FXML private ImageView songImage;
    @FXML private Label songTitle;
    @FXML private Label artistName;
    @FXML private Button playPauseButton;
    @FXML private ImageView playPauseIcon;
    @FXML private Slider progressSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label currentTime;
    @FXML private Label totalTime;
    @FXML private ListView<Song> queueListView;
    @FXML private VBox queueContainer;
    @FXML private Button likeButton;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private ImageView likeIcon;

    private AudioService audioService;
    private RMIClient rmiClient;
    private Timeline timeline;
    private boolean isDragging = false;
    private ObservableList<Song> queueItems;
    private SessionHolder sessionHolder;
    private boolean isLiked = false;

    public void initialize() {
        setupTimeline();
        setupVolumeSlider();
        setupQueueDisplay();
        setupLikeButton();
    }

    private void setupQueueDisplay() {
        queueItems = FXCollections.observableArrayList();
        queueListView.setItems(queueItems);
        queueListView.setCellFactory(lv -> new SongQueueCell());
    }

    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateProgress()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void setupVolumeSlider() {
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (audioService != null) {
                audioService.setVolume(newVal.doubleValue());
            }
        });
    }

    private void setupLikeButton() {
        likeButton.getStyleClass().add("like-button");
        updateLikeButtonState();
    }

    private void updateLikeButtonState() {
        if (isLiked) {
            likeIcon.setImage(new Image(getClass().getResourceAsStream("/com/istream/images/heart-filled.png")));
            likeButton.getStyleClass().add("liked");
        } else {
            likeIcon.setImage(new Image(getClass().getResourceAsStream("/com/istream/images/heart-outline.png")));
            likeButton.getStyleClass().remove("liked");
        }
    }

    private void updateProgress() {
        if (audioService != null && !isDragging) {
            Duration current = audioService.getCurrentPosition();
            Duration total = audioService.getTotalDuration();
            
            if (total.greaterThan(Duration.ZERO)) {
                progressSlider.setValue(current.toSeconds() / total.toSeconds() * 100);
                currentTime.setText(formatDuration(current));
                totalTime.setText(formatDuration(total));
            }
        }
    }

    private String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    @FXML
    public void handlePlayPause() {
        if (audioService != null) {
            if (audioService.isPlaying()) {
                audioService.pause();
                playPauseIcon.setImage(new Image(getClass().getResourceAsStream("/com/istream/images/play.png")));
            } else {
                audioService.resume();
                playPauseIcon.setImage(new Image(getClass().getResourceAsStream("/com/istream/images/pause.png")));
            }
        }
    }

    @FXML
    private void handlePrevious() {
        if (audioService != null) {
            audioService.playPrevious();
            updateQueueDisplay();
        }
    }

    @FXML
    private void handleNext() {
        if (audioService != null) {
            audioService.skipToNext();
            updateSongInfo(audioService.getCurrentSong());
        }
    }

    @FXML
    private void handleSliderPressed() {
        isDragging = true;
    }

    @FXML
    private void handleSliderReleased() {
        if (audioService != null) {
            Duration total = audioService.getTotalDuration();
            double percentage = progressSlider.getValue() / 100.0;
            Duration newPosition = Duration.seconds(total.toSeconds() * percentage);
            audioService.seek(newPosition);
        }
        isDragging = false;
    }

    @FXML
    private void handleSliderValueChanged() {
        if (audioService != null && isDragging) {
            Duration total = audioService.getTotalDuration();
            double percentage = progressSlider.getValue() / 100.0;
            Duration newPosition = Duration.seconds(total.toSeconds() * percentage);
            currentTime.setText(formatDuration(newPosition));
        }
    }

    @FXML
    private void handleVolumeChange() {
        if (audioService != null) {
            audioService.setVolume(volumeSlider.getValue());
        }
    }

    @FXML
    private void handleLike() {
        if (rmiClient != null && sessionHolder != null && sessionHolder.getCurrentUserId() != -1) {
            Song currentSong = audioService.getCurrentSong();
            if (currentSong != null) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        System.out.println("Toggling like status for song ID: " + currentSong.getId());
                        if (isLiked) {
                            rmiClient.unlikeSong(currentSong.getId());
                        } else {
                            rmiClient.likeSong(currentSong.getId());
                        }
                        return null;
                    }
                };

                task.setOnSucceeded(e -> {
                    ThreadManager.runOnFxThread(() -> {
                        isLiked = !isLiked;
                        updateLikeButtonState();
                    });
                });

                task.setOnFailed(e -> {
                    ThreadManager.runOnFxThread(() -> 
                        UiComponent.showError("Error", "Failed to update like status: " + task.getException().getMessage())
                    );
                });

                ThreadManager.submitTask(task);
            }
        }
    }

    public void setAudioService(AudioService audioService) {
        this.audioService = audioService;
        updateQueueDisplay();
    }

    public void setRMIClient(RMIClient rmiClient) {
        this.rmiClient = rmiClient;
    }

    public void setSessionHolder(SessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;
    }

    public void updateSongInfo(Song song) {
        if (song != null) {
            songTitle.setText(song.getTitle());
            artistName.setText(String.valueOf(song.getArtistId()));
            
            // Check if song is liked
            if (rmiClient != null && sessionHolder != null && sessionHolder.getCurrentUserId() != -1) {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        return rmiClient.isSongLiked(song.getId());
                    }
                };

                task.setOnSucceeded(e -> {
                    ThreadManager.runOnFxThread(() -> {
                        isLiked = task.getValue();
                        updateLikeButtonState();
                    });
                });

                task.setOnFailed(e -> {
                    ThreadManager.runOnFxThread(() -> 
                        UiComponent.showError("Error", "Failed to check like status: " + task.getException().getMessage())
                    );
                });

                ThreadManager.submitTask(task);
            }
            
            // Load song image
            UiComponent.loadImage(songImage, song.getCoverArtPath(), rmiClient);
            // if (rmiClient != null) {
            //     Task<Image> task = new Task<>() {
            //         @Override
            //         protected Image call() throws Exception {
            //             return rmiClient.getImage("images/song/" + song.getId() + ".png");
            //         }
            //     };

            //     task.setOnSucceeded(e -> {
            //         Image image = task.getValue();
            //         if (image != null) {
            //             ThreadManager.runOnFxThread(() -> songImage.setImage(image));
            //         }
            //     });

            //     task.setOnFailed(e -> {
            //         ThreadManager.runOnFxThread(() -> 
            //             UiComponent.showError("Error", "Failed to load song image: " + task.getException().getMessage())
            //         );
            //     });

            //     ThreadManager.submitTask(task);
            // }
            
            timeline.play();
            updateQueueDisplay();
        }
    }

    public void updateQueueDisplay() {
        if (audioService != null) {
            ThreadManager.runOnFxThread(() -> {
                queueItems.clear();
                queueItems.addAll(audioService.getSongQueue());
            });
        }
    }

    private static class SongQueueCell extends javafx.scene.control.ListCell<Song> {
        @Override
        protected void updateItem(Song song, boolean empty) {
            super.updateItem(song, empty);
            if (empty || song == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(song.getTitle() + " - ");
            }
        }
    }
} 