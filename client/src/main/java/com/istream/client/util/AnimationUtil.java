package com.istream.client.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtil {
    
    /**
     * Applies a fade-in animation to a node
     * @param node The node to animate
     * @param duration The duration of the animation in milliseconds
     */
    public static void fadeIn(Node node, int duration) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }
    
    /**
     * Applies a fade-out animation to a node
     * @param node The node to animate
     * @param duration The duration of the animation in milliseconds
     */
    public static void fadeOut(Node node, int duration) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.play();
    }
    
    /**
     * Applies a slide-in animation to a node
     * @param node The node to animate
     * @param duration The duration of the animation in milliseconds
     */
    public static void slideIn(Node node, int duration) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
        translateTransition.setFromX(-100);
        translateTransition.setToX(0);
        translateTransition.play();
    }
    
    /**
     * Applies a slide-out animation to a node
     * @param node The node to animate
     * @param duration The duration of the animation in milliseconds
     */
    public static void slideOut(Node node, int duration) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
        translateTransition.setFromX(0);
        translateTransition.setToX(-100);
        translateTransition.play();
    }
    
    /**
     * Applies a scale-in animation to a node
     * @param node The node to animate
     * @param duration The duration of the animation in milliseconds
     */
    public static void scaleIn(Node node, int duration) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(duration), node);
        scaleTransition.setFromX(0);
        scaleTransition.setFromY(0);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.play();
    }
    
    /**
     * Applies a scale-out animation to a node
     * @param node The node to animate
     * @param duration The duration of the animation in milliseconds
     */
    public static void scaleOut(Node node, int duration) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(duration), node);
        scaleTransition.setFromX(1);
        scaleTransition.setFromY(1);
        scaleTransition.setToX(0);
        scaleTransition.setToY(0);
        scaleTransition.play();
    }
    
    /**
     * Applies a pulse animation to a node
     * @param node The node to animate
     * @param duration The duration of the animation in milliseconds
     */
    public static void pulse(Node node, int duration) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(duration), node);
        scaleTransition.setFromX(1);
        scaleTransition.setFromY(1);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.play();
    }
    
    /**
     * Applies a shake animation to a node
     * @param node The node to animate
     * @param duration The duration of the animation in milliseconds
     */
    public static void shake(Node node, int duration) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
        translateTransition.setFromX(0);
        translateTransition.setToX(10);
        translateTransition.setAutoReverse(true);
        translateTransition.setCycleCount(6);
        translateTransition.play();
    }
    
    /**
     * Applies a bounce animation to a node
     * @param node The node to animate
     * @param duration The duration of the animation in milliseconds
     */
    public static void bounce(Node node, int duration) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
        translateTransition.setFromY(0);
        translateTransition.setToY(-20);
        translateTransition.setAutoReverse(true);
        translateTransition.setCycleCount(2);
        translateTransition.play();
    }
    
    /**
     * Shows a toast notification
     * @param node The node to show as a toast
     * @param duration The duration to show the toast in milliseconds
     */
    public static void showToast(Node node, int duration) {
        node.setOpacity(0);
        node.setTranslateY(20);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), node);
        slideIn.setFromY(20);
        slideIn.setToY(0);
        
        ParallelTransition show = new ParallelTransition(fadeIn, slideIn);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), node);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), node);
        slideOut.setFromY(0);
        slideOut.setToY(-20);
        
        ParallelTransition hide = new ParallelTransition(fadeOut, slideOut);
        
        SequentialTransition sequence = new SequentialTransition(
            show,
            new PauseTransition(Duration.millis(duration)),
            hide
        );
        
        sequence.play();
    }
} 