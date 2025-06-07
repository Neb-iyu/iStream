package com.istream.client.service;

import java.util.ArrayList;
import java.util.List;

import com.istream.client.controller.TopMenuController;
import com.istream.client.controller.TopMenuController.SearchResult;
import com.istream.client.controller.MainAppController;
import com.istream.client.util.ThreadManager;
import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.Song;

import javafx.concurrent.Task;

public class SearchService {
    private final RMIClient rmiClient;
    private final TopMenuController topMenuController;
    private final MainAppController mainAppController;

    public SearchService(RMIClient rmiClient, TopMenuController topMenuController, MainAppController mainAppController) {
        this.mainAppController = mainAppController;
        this.rmiClient = rmiClient;
        this.topMenuController = topMenuController;
        setupSearchHandlers();
    }

    private void setupSearchHandlers() {
        topMenuController.setOnSearchByName(() -> {
            String query = topMenuController.getSearchField().getText();
            if (query.length() >= 2) {
                performSearch(query);
            }
        });

        topMenuController.setOnResultClick(result -> {
            switch (result.getType()) {
                case "album":
                    mainAppController.loadAlbumView(result.getId());
                    break;
                case "artist":
                    mainAppController.loadArtistView(result.getId());
                    break;
                case "song":
                    try {
                        Song song = rmiClient.getSongById(result.getId());
                        if (song != null) {
                            mainAppController.playSong(song);
                        }
                    } catch (Exception e) {
                        UiComponent.showError("Error", "Failed to play song: " + e.getMessage());
                    }
                    break;
            }
        });
    }

    private void performSearch(String query) {
        Task<List<SearchResult>> task = new Task<>() {
            @Override
            protected List<SearchResult> call() {
                List<SearchResult> results = new ArrayList<>();
                try {
                    // Search albums
                    List<Album> albums = rmiClient.getAlbumsByTitle(query);
                    for (Album album : albums) {
                        Artist artist = rmiClient.getArtistById(album.getArtistId());
                        results.add(new SearchResult(
                            album.getTitle(),
                            artist != null ? artist.getName() : "Unknown Artist",
                            "album",
                            album.getId(),
                            album.getCoverArtPath()
                        ));
                    }

                    // Search artists
                    List<Artist> artists = rmiClient.getArtistsByName(query);
                    for (Artist artist : artists) {
                        results.add(new SearchResult(
                            artist.getName(),
                            "Artist",
                            "artist",
                            artist.getId(),
                            artist.getImageUrl()
                        ));
                    }

                    // Search songs
                    List<Song> songs = rmiClient.getSongsByName(query);
                    for (Song song : songs) {
                        Artist artist = rmiClient.getArtistById(song.getArtistId());
                        Album album = rmiClient.getAlbumById(song.getAlbumId());
                        results.add(new SearchResult(
                            song.getTitle(),
                            artist != null ? artist.getName() : "Unknown Artist",
                            "song",
                            song.getId(),
                            song.getCoverArtPath()
                        ));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return results;
            }
        };

        task.setOnSucceeded(event -> {
            List<SearchResult> results = task.getValue();
            topMenuController.updateSearchResults(results);
        });

        task.setOnFailed(event -> {
            // Optionally show error on UI
            // You can show an error dialog here if you want
        });

        ThreadManager.submitTask(task);
    }
}