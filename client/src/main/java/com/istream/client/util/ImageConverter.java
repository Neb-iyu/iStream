package com.istream.client.util;

import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageConverter {
    
    // Convert JavaFX Image to byte array (JPEG format)
    public static byte[] imageToBytes(Image image) throws IOException {
        if (image == null) return null;
        
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos);
        return baos.toByteArray();
    }

    // Convert byte array to JavaFX Image
    public static Image bytesToImage(byte[] imageData) {
        if (imageData == null) return null;
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            return new Image(bais);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}