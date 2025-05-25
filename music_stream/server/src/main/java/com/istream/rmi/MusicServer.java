package com.istream.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MusicServer {
    private static final int RMI_PORT = 1099;
    private static final long CLEANUP_INTERVAL = 30; // minutes
    private static ScheduledExecutorService scheduler;

    public static void main(String[] args) {
        try {
            // Set system properties for RMI
            System.setProperty("java.rmi.server.hostname", "localhost");
            
            // Create and export the MusicService object
            MusicService musicService = new MusicServiceImpl();
            
            // Create registry
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind("MusicService", musicService);
            
            // Setup cleanup scheduler
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    if (musicService instanceof MusicServiceImpl) {
                        ((MusicServiceImpl) musicService).cleanupOldSessions();
                    }
                } catch (Exception e) {
                    System.err.println("Cleanup task failed: " + e.getMessage());
                }
            }, CLEANUP_INTERVAL, CLEANUP_INTERVAL, TimeUnit.MINUTES);
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    scheduler.shutdown();
                    UnicastRemoteObject.unexportObject(musicService, true);
                    UnicastRemoteObject.unexportObject(registry, true);
                } catch (Exception e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                }
            }));
            
            System.out.println("Music server is running on port " + RMI_PORT);
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
