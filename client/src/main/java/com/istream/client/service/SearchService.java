package com.istream.client.service;

import java.util.ArrayList;
import java.util.List;

import com.istream.client.controller.TopMenuController;
import com.istream.client.controller.TopMenuController.SearchResult;
import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.Song;

public class SearchService {
    private final RMIClient rmiClient;
    private final TopMenuController topMenuController;

    public SearchService(RMIClient rmiClient, TopMenuController topMenuController) {
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
                    // Navigate to album view
                    break;
                case "artist":
                    // Navigate to artist view
                    break;
                case "song":
                    // Play song
                    break;
            }
        });
    }

    private void performSearch(String query) {
        try {
            List<SearchResult> results = new ArrayList<>();
            
            // Search albums
            List<Album> albums = rmiClient.getAlbumsByTitle(query);
            for (Album album : albums) {
                try {
                    Artist artist = rmiClient.getArtistById(album.getArtistId());
                    results.add(new SearchResult(
                        album.getTitle(),
                        artist != null ? artist.getName() : "Unknown Artist",
                        "album",
                        album.getId(),
                        album.getCoverArtPath()
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                try {
                    Artist artist = rmiClient.getArtistById(song.getArtistId());
                    Album album = rmiClient.getAlbumById(song.getAlbumId());
                    results.add(new SearchResult(
                        song.getTitle(),
                        artist != null ? artist.getName() : "Unknown Artist",
                        "song",
                        song.getId(),
                        song.getCoverArtPath()
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            topMenuController.updateSearchResults(results);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error
        }
    }
} 