package com.istream.client.util;

import java.security.PrivilegedActionException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;

import com.istream.model.Song;
import com.istream.model.Artist;
import com.istream.model.Album;
import com.istream.client.view.*;
import com.istream.client.service.AudioService;
import com.istream.rmi.MusicService;
import com.istream.client.controller.MainAppController;

import javafx.concurrent.Task;

import com.istream.client.service.RMIClient;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;

import java.util.List;

import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ContextMenuEvent;

public class UiComponent {
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
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
                Platform.runLater(() -> imageView.setImage(image));
            }
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                showError("Error", "Failed to load image: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }

    public static void createSongRow(List<Song> songs, HBox container, RMIClient rmiClient, MainAppController mainAppController) {
        container.getChildren().clear();
        for (Song song : songs) {
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
            loadImage(imageView, "images/song/" + song.getId() + ".png", rmiClient);

            Label titleLabel = new Label(song.getTitle());
            titleLabel.getStyleClass().add("title");
            Label artistLabel = new Label();
            try {
                Artist artist = rmiClient.getArtistById(song.getArtistId());
                artistLabel.setText(artist.getName());
                artistLabel.getStyleClass().add("subtitle");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Button playButton = new Button("Play");
            playButton.getStyleClass().add("primary");
            playButton.setOnAction(e -> playSongAndClearQueue(song, rmiClient, mainAppController));

            songBox.getChildren().addAll(imageView, titleLabel, artistLabel, playButton);
            container.getChildren().add(songBox);
        }
    }

    public static void createAlbumRow(List<Album> albums, HBox container, RMIClient rmiClient, MainAppController mainAppController) {
        container.getChildren().clear();
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
            loadImage(imageView, "images/album/" + album.getId() + ".png", rmiClient);

            Label titleLabel = new Label(album.getTitle());
            titleLabel.getStyleClass().add("title");
            
            Label artistLabel = new Label();
            try {
                Artist artist = rmiClient.getArtistById(album.getArtistId());
                artistLabel.setText(artist.getName());
                artistLabel.getStyleClass().add("subtitle");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Button viewButton = new Button("View");
            viewButton.getStyleClass().add("primary");
            viewButton.setOnAction(e -> mainAppController.loadAlbumView(album.getId()));

            albumBox.getChildren().addAll(imageView, titleLabel, artistLabel, viewButton);
            container.getChildren().add(albumBox);
        }
    }

    public static void createArtistRow(List<Artist> artists, HBox container, RMIClient rmiClient, MainAppController mainAppController) {
        container.getChildren().clear();
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
            loadImage(imageView, "images/artist/" + artist.getId() + ".png", rmiClient);

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
            mainAppController.playSong(song);
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                showError("Error", "Failed to play song: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
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
                .thenRun(() -> Platform.runLater(() -> 
                    showNotification("Added to Queue", song.getTitle() + " has been added to the queue")
                ))
                .exceptionally(ex -> {
                    Platform.runLater(() -> 
                        showError("Error", "Failed to add song to queue: " + ex.getMessage())
                    );
                    return null;
                });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> 
                showError("Error", "Failed to load song: " + task.getException().getMessage())
            );
        });

        new Thread(task).start();
    }

    private static void handleSongPlay(Song song, RMIClient rmiClient, MainAppController mainAppController) {
        try {
            byte[] audioData = rmiClient.streamSong(song.getId());
            mainAppController.playSong(song);
        } catch (Exception e) {
            showError("Error", "Failed to play song: " + e.getMessage());
        }
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
        Platform.runLater(() -> {
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
                    .thenRun(() -> Platform.runLater(() -> 
                        showNotification("Added to Queue", song.getTitle() + " will play next")
                    ));
            });

            task.setOnFailed(event -> {
                Platform.runLater(() -> 
                    showError("Error", "Failed to add song to queue: " + task.getException().getMessage())
                );
            });

            new Thread(task).start();
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
                    Platform.runLater(() -> 
                        showError("Error", "Failed to add song to queue: " + task.getException().getMessage())
                    );
                });

                new Thread(task).start();
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
}