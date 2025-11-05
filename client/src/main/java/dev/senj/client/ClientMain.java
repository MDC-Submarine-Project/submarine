package dev.senj.client;

import dev.senj.common.Constants;
import dev.senj.common.ObjectListener;
import dev.senj.common.Position;
import dev.senj.common.Utils;

import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) {
        System.out.println(Utils.greeting("Client"));

        ObjectListener objectListener = ObjectListener.getInstance();

        // Try to initialize as a server first, then as a client if server initialization fails
        boolean connected = false;
        int maxRetries = 30; // Try for about 30 seconds
        int retryCount = 0;

        // First try to initialize as a server
        try {
            System.out.println("Attempting to initialize as a server on port " + Constants.DEFAULT_PORT);
            objectListener.initializeAsServer(Constants.DEFAULT_PORT);
            connected = true;
            System.out.println("Successfully initialized as a server!");
        } catch (IOException e) {
            System.out.println("Failed to initialize as a server: " + e.getMessage());
            System.out.println("Trying to connect as a client instead...");

            // If server initialization fails, try to connect as a client with retries
            while (!connected) {
                try {
                    System.out.println("Attempting to connect to server at " + Constants.DEFAULT_HOST + ":" + Constants.DEFAULT_PORT +
                                      " (Attempt " + (retryCount + 1) + " of " + maxRetries + ")");
                    objectListener.initializeAsClient(Constants.DEFAULT_HOST, Constants.DEFAULT_PORT);
                    connected = true;
                    System.out.println("Successfully connected to server as a client!");
                } catch (IOException ex) {
                    retryCount++;
                    if (retryCount >= maxRetries) {
                        System.err.println("Failed to initialize network after " + maxRetries + " attempts: " + ex.getMessage());
                        return;
                    }
                    System.out.println("Connection attempt failed. Retrying in 1 second...");
                    try {
                        Thread.sleep(1000); // Wait 1 second before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.err.println("Retry interrupted: " + ie.getMessage());
                        return;
                    }
                }
            }
        }

        // Add shutdown hook to clean up resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Client shutting down...");
            objectListener.stop();
        }));

        // Start the object listener
        objectListener.start();

        // Create a local position object to receive updates
        Position position = Position.getInstance();

        // Register the position object with the object listener
        objectListener.put(
                new ObjectListener.DestinationMapping("position"),
                position
        );

        int i = 0;
        while (true) {
            System.out.println("client: still running, position: " + position);
            i++;
            try {
                Thread.sleep(1000);

                if (i > 10) {
                    break;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("client interrupted, exiting.");
                break;
            }
        }
        Utils.setupClosing("client");
    }
}
