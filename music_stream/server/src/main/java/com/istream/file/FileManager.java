package com.istream.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.util.HashSet;

public class FileManager {
    private static final String BASE_DIR = "music_stream/server/src/main/resources/music/"; // Base directory for music files

    public FileManager() {
        // Ensure the base directory exists
        File dir = new File(BASE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public byte[] getSongData(int songId) {
        Path path = Paths.get(BASE_DIR + songId + ".mp3");
        if (Files.exists(path)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public File getSongFile(int songId) {
        return new File(BASE_DIR + songId + ".mp3");
    }

    public HashSet<byte[]> getAllSongFiles() {
        HashSet<byte[]> files = new HashSet<>();
        File dir = new File(BASE_DIR);
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".mp3")) {
                Path path = Paths.get(file.getAbsolutePath());
                try {
                    byte[] data = Files.readAllBytes(path);
                    files.add(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return files;
    }
    public void storeSongData(int songId, byte[] data) throws IOException {
        File file = getSongFile(songId);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
