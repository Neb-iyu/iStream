package com.istream.file;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import javax.imageio.ImageIO;

public class FileManager {
    private static final String BASE_DIR = "server/src/main/resources/"; // Base directory for music files

    public FileManager() {
        ensureDirectoryExists(BASE_DIR + "images/");
        ensureDirectoryExists(BASE_DIR + "songs/");
    }

    private void ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public byte[] getSongData(int songId) {
        Path path = Paths.get(BASE_DIR + "songs/" + songId + ".mp3");
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
        return new File(BASE_DIR + "songs/" + songId + ".mp3");
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

    public byte[] getImage(String path) throws IOException {
        Path filePath = Paths.get(BASE_DIR + path);
        if (Files.exists(filePath)) {
            //System.out.println("Image exists: " + path);
            BufferedImage image = ImageIO.read(filePath.toFile());
            String ext = getFileExtension(filePath.getFileName().toString());
        if (image == null) {
            System.out.println("ImageIO.read returned null for: " + filePath);
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Use the actual file extension for encoding
        ImageIO.write(image, ext, baos);
            return baos.toByteArray();
        }
        else {
            System.out.println("Image does not exist: " + filePath);
            BufferedImage defaultImage = ImageIO.read(getClass().getResourceAsStream(BASE_DIR + "images/default.png"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(defaultImage, "png", baos);
            return baos.toByteArray();
        }
    }
    public void saveImage(String path, byte[] image) throws IOException {
        Path filePath = Paths.get(BASE_DIR + "images/" + path);
        try(InputStream is = new ByteArrayInputStream(image)) {
            BufferedImage bi = ImageIO.read(is);
            ImageIO.write(bi, "jpeg", filePath.toFile());
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "png"; 
    }
    
}
