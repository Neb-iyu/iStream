package com.istream.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.util.regex.Pattern;



public class Security {
    private static final int BCRYPT_STRENGTH = 12;
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    
    // Password hashing
    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(BCRYPT_STRENGTH, password.toCharArray());
    }
    
    public static boolean verifyPassword(String password, String hash) {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified;
    }
    
    // File path sanitization
    public static String sanitizeFilePath(String input) {
        if (!FILENAME_PATTERN.matcher(input).matches()) {
            throw new SecurityException("Invalid filename");
        }
        return input.replace("..", ""); // Prevent path traversal
    }
    
    // RMI object validation
    public static void validateRemoteObject(Object obj) {
        if (obj == null || !obj.getClass().getName().startsWith("com.musicstreamer.")) {
            throw new SecurityException("Invalid remote object");
        }
    }
}