package com.istream.client.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;

public class ThreadManager {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    
    public static void submitTask(Runnable task) {
        executorService.submit(task);
    }
    
    public static void runOnFxThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
    
    public static void shutdown() {
        executorService.shutdown();
    }
} 