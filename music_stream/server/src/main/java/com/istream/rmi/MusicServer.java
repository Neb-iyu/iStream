package com.istream.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
public class MusicServer {
    public static void main(String[] args) {
        
    
        try {
            // Create and export the MusicService object
            MusicService musicService = new MusicServiceImpl();
            
            // Bind the service to the RMI registry
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MusicService", musicService);
            
            System.out.println("Music server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
