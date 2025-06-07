package com.istream.client.util;

import java.util.List;

import com.istream.client.controller.MainAppController;
import com.istream.client.service.AudioService;
import com.istream.client.service.RMIClient;
import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.Song;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class UiComponent {
    public static void showError(String title, String message) {
        ThreadManager.runOnFxThread(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.getDialogPane().getStyleClass().add("dialog-pane");
            alert.showAndWait();
        });
    }

    public static String showInputDialog(String title, String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        
        return dialog.showAndWait().orElse(null);
    }

    public static void loadImage(ImageView imageView, String imagePath, RMIClient rmiClient) {
        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                return rmiClient.getImage(imagePath);
            }
        };

        task.setOnSucceeded(e -> {
            Image image = task.getValue();
            if (image != null) {
                ThreadManager.runOnFxThread(() -> imageView.setImage(image));
            }
            else {
                ThreadManager.runOnFxThread(() -> {
                    Image placeholder = new Image(UiComponent.class.getResourceAsStream("/default.png"));
                    imageView.setImage(placeholder);                    ;
                });
            }
        });

        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> 
                showError("Error", "Failed to load image: " + task.getException().getMessage())
            );
        });

        ThreadManager.submitTask(task);
    }

    public static void createSongRow(List<Song> songs, HBox container, RMIClient rmiClient, MainAppController mainAppController) {
        ThreadManager.runOnFxThread(() -> container.getChildren().clear());
        
        if (songs == null || songs.isEmpty()) {
            createEmptyState(container, "No songs found");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Song song : songs) {
                    VBox songBox = createSongBox(song, rmiClient, mainAppController);
                    ThreadManager.runOnFxThread(() -> container.getChildren().add(songBox));
                }
                return null;
            }
        };

        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> {
                createEmptyState(container, "Failed to load songs: " + task.getException().getMessage());
                showError("Error", "Failed to load songs: " + task.getException().getMessage());
            });
        });

        ThreadManager.submitTask(task);
    }

    private static VBox createSongBox(Song song, RMIClient rmiClient, MainAppController mainAppController) {
        VBox songBox = new VBox(5);
        songBox.setPadding(new Insets(10));
        songBox.getStyleClass().add("card");
        songBox.setPrefWidth(200);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("image-view");
        imageView.setCursor(Cursor.HAND);
        imageView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                playSongAndClearQueue(song, rmiClient, mainAppController);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                ContextMenu contextMenu = createSongContextMenu(song, rmiClient, mainAppController);
                contextMenu.show(imageView, e.getScreenX(), e.getScreenY());
            }
        });
        loadImage(imageView, song.getCoverArtPath(), rmiClient);

        Label titleLabel = new Label(song.getTitle());
        titleLabel.getStyleClass().add("title");
        
        Label artistLabel = new Label();
        Task<Artist> artistTask = new Task<>() {
            @Override
            protected Artist call() throws Exception {
                return rmiClient.getArtistById(song.getArtistId());
            }
        };
        
        artistTask.setOnSucceeded(e -> {
            Artist artist = artistTask.getValue();
            ThreadManager.runOnFxThread(() -> {
                artistLabel.setText(artist.getName());
                artistLabel.getStyleClass().add("subtitle");
            });
        });

        Button playButton = new Button("Play");
        playButton.getStyleClass().add("primary");
        playButton.setOnAction(e -> playSongAndClearQueue(song, rmiClient, mainAppController));

        songBox.getChildren().addAll(imageView, titleLabel, artistLabel, playButton);
        ThreadManager.submitTask(artistTask);
        return songBox;
    }

    private static void createEmptyState(HBox container, String message) {
        ThreadManager.runOnFxThread(() -> {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(20));
            emptyBox.getStyleClass().add("empty-state");
            
            Label emptyLabel = new Label(message);
            emptyLabel.getStyleClass().add("empty-label");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b3b3b3;");
            
            emptyBox.getChildren().add(emptyLabel);
            container.getChildren().add(emptyBox);
        });
    }

    public static void createAlbumRow(List<Album> albums, HBox container, RMIClient rmiClient, MainAppController mainAppController) {
        ThreadManager.runOnFxThread(container.getChildren()::clear);

        if (albums == null || albums.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(20));
            emptyBox.getStyleClass().add("empty-state");

            Label emptyLabel = new Label("No albums found");
            emptyLabel.getStyleClass().add("empty-label");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b3b3b3;");

            emptyBox.getChildren().add(emptyLabel);
            ThreadManager.runOnFxThread(() -> container.getChildren().add(emptyBox));
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                for (Album album : albums) {
                    VBox albumBox = new VBox(5);
                    albumBox.setPadding(new Insets(10));
                    albumBox.getStyleClass().add("card");
                    albumBox.setPrefWidth(200);

                    ImageView imageView = new ImageView();
                    imageView.setFitHeight(150);
                    imageView.setFitWidth(150);
                    imageView.setPreserveRatio(true);
                    imageView.getStyleClass().add("image-view");
                    imageView.setCursor(Cursor.HAND);
                    imageView.setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            mainAppController.loadAlbumView(album.getId());
                        } else if (e.getButton() == MouseButton.SECONDARY) {
                            ContextMenu contextMenu = createAlbumContextMenu(album, rmiClient, mainAppController);
                            contextMenu.show(imageView, e.getScreenX(), e.getScreenY());
                        }
                    });
                    loadImage(imageView, album.getCoverArtPath(), rmiClient);
                    //System.out.println("Image width: " + imageView.getFitWidth() + ", height: " + imageView.getFitHeight());

                    Label titleLabel = new Label(album.getTitle());
                    titleLabel.getStyleClass().add("title");
                    
                    Label artistLabel = new Label();
                    try {
                        Artist artist = rmiClient.getArtistById(album.getArtistId());
                        ThreadManager.runOnFxThread(() -> {
                            artistLabel.setText(artist.getName());
                            artistLabel.getStyleClass().add("subtitle");
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        ThreadManager.runOnFxThread(() -> artistLabel.setText("Unknown Artist"));
                    }

                    Button viewButton = new Button("View");
                    viewButton.getStyleClass().add("primary");
                    viewButton.setOnAction(e -> mainAppController.loadAlbumView(album.getId()));

                    ThreadManager.runOnFxThread(() -> albumBox.getChildren().addAll(imageView, titleLabel, artistLabel, viewButton));
                    ThreadManager.runOnFxThread(() -> container.getChildren().add(albumBox));
                }
                return null;
            }
        };

        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> {
                VBox emptyBox = new VBox(10);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(20));
                emptyBox.getStyleClass().add("empty-state");

                Label emptyLabel = new Label("Failed to load albums: " + task.getException().getMessage());
                emptyLabel.getStyleClass().add("empty-label");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b3b3b3;");

                emptyBox.getChildren().add(emptyLabel);
                container.getChildren().add(emptyBox);
                showError("Error", "Failed to load albums: " + task.getException().getMessage());
            });
        });

        ThreadManager.submitTask(task);
    }

    public static void createArtistRow(List<Artist> artists, HBox container, RMIClient rmiClient, MainAppController mainAppController) {
        container.getChildren().clear();
        
        if (artists == null || artists.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(20));
            emptyBox.getStyleClass().add("empty-state");
            
            Label emptyLabel = new Label("No artists found");
            emptyLabel.getStyleClass().add("empty-label");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b3b3b3;");
            
            emptyBox.getChildren().add(emptyLabel);
            container.getChildren().add(emptyBox);
            return;
        }

        for (Artist artist : artists) {
            VBox artistBox = new VBox(5);
            artistBox.setPadding(new Insets(10));
            artistBox.getStyleClass().add("card");
            artistBox.setPrefWidth(200);

            ImageView imageView = new ImageView();
            imageView.setFitHeight(150);
            imageView.setFitWidth(150);
            imageView.setPreserveRatio(true);
            imageView.getStyleClass().add("image-view");
            imageView.setCursor(Cursor.HAND);
            imageView.setOnMouseClicked(e -> mainAppController.loadArtistView(artist.getId()));
            loadImage(imageView, artist.getImageUrl(), rmiClient);

            Label nameLabel = new Label(artist.getName());
            nameLabel.getStyleClass().add("title");

            Button viewButton = new Button("View");
            viewButton.getStyleClass().add("primary");
            viewButton.setOnAction(e -> mainAppController.loadArtistView(artist.getId()));

            artistBox.getChildren().addAll(imageView, nameLabel, viewButton);
            container.getChildren().add(artistBox);
        }
    }

    public static void playSongAndClearQueue(Song song, RMIClient rmiClient, MainAppController mainAppController) {
        AudioService audioService = mainAppController.getAudioService();
        Task<byte[]> task = new Task<>() {
            @Override
            protected byte[] call() throws Exception {
                return rmiClient.streamSong(song.getId());
            }
        };

        task.setOnSucceeded(e -> {
            byte[] audioData = task.getValue();
            audioService.clearQueue();
            audioService.playSong(song, audioData);
            mainAppController.getPlayerBarController().updateSongInfo(song);
        });

        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> 
                showError("Error", "Failed to play song: " + task.getException().getMessage())
            );
        });

        ThreadManager.submitTask(task);
    }

    public static void addSongToQueueAsync(Song song, RMIClient rmiClient, MainAppController mainAppController) {
        AudioService audioService = mainAppController.getAudioService();
        Task<byte[]> task = new Task<>() {
            @Override
            protected byte[] call() throws Exception {
                return rmiClient.streamSong(song.getId());
            }
        };

        task.setOnSucceeded(e -> {
            byte[] audioData = task.getValue();
            audioService.addToQueueAsync(song, audioData)
                .thenRun(() -> ThreadManager.runOnFxThread(() -> 
                    showNotification("Added to Queue", song.getTitle() + " has been added to the queue")
                ))
                .exceptionally(ex -> {
                    ThreadManager.runOnFxThread(() -> 
                        showError("Error", "Failed to add song to queue: " + ex.getMessage())
                    );
                    return null;
                });
        });

        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> 
                showError("Error", "Failed to load song: " + task.getException().getMessage())
            );
        });

        ThreadManager.submitTask(task);
    }

    private static void handleSongPlay(Song song, RMIClient rmiClient, MainAppController mainAppController) {
        AudioService audioService = mainAppController.getAudioService();
        Task<byte[]> task = new Task<>() {
            @Override
            protected byte[] call() throws Exception {
                return rmiClient.streamSong(song.getId());
            }
        };

        task.setOnSucceeded(e -> {
            byte[] audioData = task.getValue();
            audioService.playSong(song, audioData);
            mainAppController.getPlayerBarController().updateSongInfo(song);
        });

        task.setOnFailed(e -> {
            ThreadManager.runOnFxThread(() -> 
                showError("Error", "Failed to load song: " + task.getException().getMessage())
            );
        });

        ThreadManager.submitTask(task);
    }

    private static void addNext(Song song, RMIClient rmiClient, MainAppController mainAppController) {
        //TODO: Implement 
    }

    private static void showPlaylist(Song song, RMIClient rmiClient, MainAppController mainAppController) {
        //TODO: Implement
    }
    public static ScrollPane configureScrollPane(ScrollPane scrollPane) {
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    public static void showNotification(String title, String message) {
        ThreadManager.runOnFxThread(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().getStyleClass().add("dialog-pane");
            alert.showAndWait();
        });
    }

    private static ContextMenu createSongContextMenu(Song song, RMIClient rmiClient, MainAppController mainAppController) {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem playNow = new MenuItem("▶ Play Now");
        playNow.setOnAction(e -> playSongAndClearQueue(song, rmiClient, mainAppController));
        
        MenuItem addToQueue = new MenuItem("Add to Queue");
        addToQueue.setOnAction(e -> addSongToQueueAsync(song, rmiClient, mainAppController));
        
        MenuItem playNext = new MenuItem("Play Next");
        playNext.setOnAction(e -> {
            AudioService audioService = mainAppController.getAudioService();
            Task<byte[]> task = new Task<>() {
                @Override
                protected byte[] call() throws Exception {
                    return rmiClient.streamSong(song.getId());
                }
            };

            task.setOnSucceeded(event -> {
                byte[] audioData = task.getValue();
                audioService.addToQueueAsync(song, audioData)
                    .thenRun(() -> ThreadManager.runOnFxThread(() -> 
                        showNotification("Added to Queue", song.getTitle() + " will play next")
                    ));
            });

            task.setOnFailed(event -> {
                ThreadManager.runOnFxThread(() -> 
                    showError("Error", "Failed to add song to queue: " + task.getException().getMessage())
                );
            });

            ThreadManager.submitTask(task);
        });
        
        MenuItem addToPlaylist = new MenuItem("Add to Playlist");
        addToPlaylist.setOnAction(e -> {
            // TODO: Implement add to playlist functionality
            showNotification("Coming Soon", "Add to playlist functionality will be available soon!");
        });

        contextMenu.getItems().addAll(playNow, addToQueue, playNext, addToPlaylist);
        return contextMenu;
    }

    private static ContextMenu createAlbumContextMenu(Album album, RMIClient rmiClient, MainAppController mainAppController) {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem playAlbum = new MenuItem("Play Album");
        playAlbum.setOnAction(e -> playAlbum(album, rmiClient, mainAppController));
        
        MenuItem addToQueue = new MenuItem("Add Album to Queue");
        addToQueue.setOnAction(e -> {
            List<Song> songs = album.getSongs();
            for (Song song : songs) {
                addSongToQueueAsync(song, rmiClient, mainAppController);
            }
            showNotification("Added to Queue", album.getTitle() + " has been added to the queue");
        });
        
        MenuItem playNext = new MenuItem("Play Album Next");
        playNext.setOnAction(e -> {
            List<Song> songs = album.getSongs();
            AudioService audioService = mainAppController.getAudioService();
            
            for (Song song : songs) {
                Task<byte[]> task = new Task<>() {
                    @Override
                    protected byte[] call() throws Exception {
                        return rmiClient.streamSong(song.getId());
                    }
                };

                task.setOnSucceeded(event -> {
                    byte[] audioData = task.getValue();
                    audioService.addToQueueAsync(song, audioData);
                });

                task.setOnFailed(event -> {
                    ThreadManager.runOnFxThread(() -> 
                        showError("Error", "Failed to add song to queue: " + task.getException().getMessage())
                    );
                });

                ThreadManager.submitTask(task);
            }
            showNotification("Added to Queue", album.getTitle() + " will play next");
        });

        contextMenu.getItems().addAll(playAlbum, addToQueue, playNext);
        return contextMenu;
    }

    private static void playAlbum(Album album, RMIClient rmiClient, MainAppController mainAppController) {
        List<Song> songs = album.getSongs();
        if (!songs.isEmpty()) {
            // Play the first song and add the rest to queue
            Song firstSong = songs.get(0);
            playSongAndClearQueue(firstSong, rmiClient, mainAppController);
            
            // Add remaining songs to queue
            for (int i = 1; i < songs.size(); i++) {
                addSongToQueueAsync(songs.get(i), rmiClient, mainAppController);
            }
        }
    }

    // Add a new method for handling empty playlist lists
    public static void createEmptyPlaylistMessage(VBox container) {
        container.getChildren().clear();
        
        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(20));
        emptyBox.getStyleClass().add("empty-state");
        
        Label emptyLabel = new Label("No playlists found");
        emptyLabel.getStyleClass().add("empty-label");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b3b3b3;");
        
        emptyBox.getChildren().add(emptyLabel);
        container.getChildren().add(emptyBox);
    }

    // Add a new method for handling empty liked songs in HBox
    public static void createEmptyLikedSongsMessage(HBox container) {
        container.getChildren().clear();
        
        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(20));
        emptyBox.getStyleClass().add("empty-state");
        
        Label emptyLabel = new Label("No liked songs yet");
        emptyLabel.getStyleClass().add("empty-label");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b3b3b3;");
        
        emptyBox.getChildren().add(emptyLabel);
        container.getChildren().add(emptyBox);
    }
    public static void showSuccess(String title, String message) {
        ThreadManager.runOnFxThread(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().getStyleClass().add("dialog-pane");
            alert.showAndWait();
        });
    }

    public static void createAlbumSongList(List<Song> songs, VBox container, RMIClient rmiClient, MainAppController mainAppController) {
        container.getChildren().clear();
        
        if (songs == null || songs.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(20));
            emptyBox.getStyleClass().add("empty-state");
            
            Label emptyLabel = new Label("No songs found in this album");
            emptyLabel.getStyleClass().add("empty-label");
            container.getChildren().add(emptyBox);
            return;
        }

        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            HBox songItem = new HBox();
            songItem.getStyleClass().addAll("song-item", "fade-in");
            songItem.setAlignment(Pos.CENTER_LEFT);
            songItem.setPadding(new Insets(12, 16, 12, 16));
            songItem.setSpacing(15);

            // Song number
            Label numberLabel = new Label(String.valueOf(i + 1));
            numberLabel.getStyleClass().add("song-number");
            numberLabel.setMinWidth(30);

            // Song image
            ImageView songImage = new ImageView();
            songImage.setFitWidth(40);
            songImage.setFitHeight(40);
            songImage.getStyleClass().add("song-image");
            loadImage(songImage, song.getCoverArtPath(), rmiClient);
            System.out.println("Imagewidth" + songImage.getFitWidth() + " height: " + songImage.getFitHeight());
            

            // Song info
            VBox songInfo = new VBox(4);
            songInfo.getStyleClass().add("song-info");
            
            Label titleLabel = new Label(song.getTitle());
            titleLabel.getStyleClass().add("song-title");
            titleLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            Label artistLabel = new Label();
            try {
                Artist artist = rmiClient.getArtistById(song.getArtistId());
                artistLabel.setText(artist.getName());
            } catch (Exception e) {
                artistLabel.setText("Unknown Artist");
            }
            artistLabel.getStyleClass().add("song-artist");

            songInfo.getChildren().addAll(titleLabel, artistLabel);

            // Song duration
            Label durationLabel = new Label(formatDuration(song.getDuration()));
            durationLabel.getStyleClass().add("song-duration");

            // Action buttons
            HBox actionButtons = new HBox(10);
            actionButtons.getStyleClass().add("song-actions");
            actionButtons.setAlignment(Pos.CENTER_RIGHT);

            Button playButton = new Button("▶");
            playButton.getStyleClass().add("song-action-button");
            playButton.setOnAction(e -> playSongAndClearQueue(song, rmiClient, mainAppController));

            Button addToQueueButton = new Button("+");
            addToQueueButton.getStyleClass().add("song-action-button");
            addToQueueButton.setOnAction(e -> addSongToQueueAsync(song, rmiClient, mainAppController));

            actionButtons.getChildren().addAll(playButton, addToQueueButton);

            // Add all components to song item
            songItem.getChildren().addAll(numberLabel, songImage, songInfo, durationLabel, actionButtons);

            // Add context menu
            songItem.setOnContextMenuRequested(e -> {
                ContextMenu contextMenu = createSongContextMenu(song, rmiClient, mainAppController);
                contextMenu.show(songItem, e.getScreenX(), e.getScreenY());
            });

            // Play song on click
            songItem.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                    playSongAndClearQueue(song, rmiClient, mainAppController);
                }
            });

            container.getChildren().add(songItem);

            // Animate in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), songItem);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), songItem);
            slideIn.setFromY(20);
            slideIn.setToY(0);
            
            ParallelTransition transition = new ParallelTransition(fadeIn, slideIn);
            transition.setDelay(Duration.millis(i * 50));
            transition.play();
        }
    }

    private static String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
}