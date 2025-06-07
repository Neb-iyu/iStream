package com.istream.client.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

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
        if (imageData == null) {
            System.out.println("Image data is null");
            return null;
        }
        //System.out.println("Converting byte array to image, size: " + imageData.length);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            Image img = new Image(bais);
            //System.out.println("Loaded image: width=" + img.getWidth() + ", height=" + img.getHeight());
            return img;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}