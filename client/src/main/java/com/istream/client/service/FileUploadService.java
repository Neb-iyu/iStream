package com.istream.client.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileUploadService {
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB in bytes
    private static final Set<String> ALLOWED_AUDIO_TYPES = new HashSet<>(Arrays.asList(
        "audio/mpeg",      // .mp3
        "audio/wav",       // .wav
        "audio/ogg",       // .ogg
        "audio/mp4",       // .m4a
        "audio/aac"        // .aac
    ));

    public static class FileValidationResult {
        private final boolean isValid;
        private final String errorMessage;

        public FileValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static FileValidationResult validateFile(File file) {
        // Check if file exists
        if (!file.exists()) {
            return new FileValidationResult(false, "File does not exist");
        }

        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            return new FileValidationResult(false, "File size exceeds 50MB limit");
        }

        // Check file type
        try {
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null || !ALLOWED_AUDIO_TYPES.contains(mimeType)) {
                // Fallback: check extension
                String name = file.getName().toLowerCase();
                if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg") ||
                    name.endsWith(".m4a") || name.endsWith(".aac")) {
                    return new FileValidationResult(true, null);
                }
                return new FileValidationResult(false, "Only audio files are allowed (MP3, WAV, OGG, M4A, AAC)");
            }
        } catch (IOException e) {
            return new FileValidationResult(false, "Error checking file type: " + e.getMessage());
        }

        return new FileValidationResult(true, null);
    }

    public static byte[] readFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}