package com.istream.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.istream.model.Album;
import com.istream.model.Artist;
import com.istream.model.Song;

public class DummyDataService {
    private static final String[] ARTIST_NAMES = {
        "Taylor Swift", "Ed Sheeran", "The Weeknd", "Billie Eilish", 
        "Drake", "Ariana Grande", "Post Malone", "Dua Lipa", "Playboi Carti", "Frank Ocean"
    };

    private static final String[] ALBUM_TITLES = {
        "Midnights", "Divide", "After Hours", "Happier Than Ever",
        "Scorpion", "Positions", "Hollywood's Bleeding", "Future Nostalgia", "Whole Lotta Red", "Blonde"
    };

    private static final Map<String, String[]> ALBUM_SONGS = Map.of(
        "Midnights", new String[] {"Anti-Hero", "Lavender Haze", "Midnight Rain"},
        "Divide", new String[] {"Shape of You", "Castle on the Hill", "Perfect"},
        "After Hours", new String[] {"Blinding Lights", "Save Your Tears", "In Your Eyes"},
        "Happier Than Ever", new String[] {"Bad Guy", "Therefore I Am", "My Future"},
        "Scorpion", new String[] {"God's Plan", "In My Feelings", "Nice For What"},
        "Positions", new String[] {"34+35", "Positions", "POV"},
        "Hollywood's Bleeding", new String[] {"Circles", "Sunflower", "Goodbyes"},
        "Future Nostalgia", new String[] {"Don't Start Now", "Physical", "Break My Heart"},
        "Whole Lotta Red", new String[] {"Rockstar Made", "Stop Breathing", "M3tamorphosis"},
        "Blonde", new String[] {"Nights", "Ivy", "Pink + White"}
    );

    private static final String[] GENRES = {
        "Pop", "R&B", "Hip Hop", "Rock", "Electronic", "Jazz", "Classical"
    };

    public static List<Artist> generateArtists() {
        List<Artist> artists = new ArrayList<>();
        for (int i = 0; i < ARTIST_NAMES.length; i++) {
            artists.add(new Artist(
                i + 1,
                ARTIST_NAMES[i],
                "/images/artists/artist" + (i + 1) + ".jpg",
                new ArrayList<>(),
                new ArrayList<>()
            ));
        }
        return artists;
    }

    public static List<Album> generateAlbums(List<Artist> artists) {
        List<Album> albums = new ArrayList<>();
        for (int i = 0; i < ALBUM_TITLES.length; i++) {
            Artist artist = artists.get(i % artists.size());
            albums.add(new Album(
                i + 1,
                ALBUM_TITLES[i],
                artist.getId(),
                "/images/albums/album" + (i + 1) + ".jpg",
                new ArrayList<>()
            ));
        }
        return albums;
    }

    public static List<Song> generateSongs(List<Album> albums, List<Artist> artists) {
        List<Song> songs = new ArrayList<>();
        int songId = 1;
        
        for (Album album : albums) {
            String[] albumSongs = ALBUM_SONGS.get(album.getTitle());
            if (albumSongs != null) {
                for (String songTitle : albumSongs) {
                    songs.add(new Song(
                        songId++,
                        songTitle,
                        album.getArtistId(),
                        album.getId(),
                        GENRES[songId % GENRES.length],
                        180 + (songId * 30), // 3-6 minutes
                        "/songs/" + songId + ".mp3",
                        "/images/songs/song" + songId + ".jpg",
                        2020 + (songId % 4)
                    ));
                }
            }
        }
        return songs;
    }

    public static void populateRMIClient(RMIClient rmiClient) {
        try {
            // First add artists to get their IDs
            List<Artist> artists = generateArtists();
            for (Artist artist : artists) {
                rmiClient.addArtist(artist);
            }

            // Then add albums with proper artist IDs
            List<Album> albums = generateAlbums(artists);
            for (Album album : albums) {
                rmiClient.addAlbum(album);
            }

            // Finally add songs with proper artist and album IDs
            List<Song> songs = generateSongs(albums, artists);
            for (Song song : songs) {
                // Create a dummy byte array for the song data
                byte[] dummyData = new byte[1024]; // 1KB dummy data
                rmiClient.uploadSong(song, dummyData, "Artist " + (song.getArtistId() % artists.size()), "Album " + (song.getAlbumId() % albums.size()));
            }

            System.out.println("Successfully populated database with dummy data:");
            System.out.println("- " + songs.size() + " songs");
            System.out.println("- " + albums.size() + " albums");
            System.out.println("- " + artists.size() + " artists");
        } catch (Exception e) {
            System.err.println("Error populating database with dummy data: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 