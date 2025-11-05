package dev.senj.server;

import dev.senj.common.Constants;
import dev.senj.common.ObjectListener;
import dev.senj.common.Position;
import dev.senj.common.Utils;

import java.io.IOException;
import java.util.UUID;

public class ServerMain {
    public static void main(String[] args) {
        ObjectListener objectListener = ObjectListener.getInstance();

        // Initialize the network as a server with retries
        boolean initialized = false;
        int maxRetries = 30; // Try for about 30 seconds
        int retryCount = 0;

        while (!initialized && retryCount < maxRetries) {
            try {
                System.out.println("Attempting to initialize server on port " + Constants.DEFAULT_PORT + 
                                  " (Attempt " + (retryCount + 1) + " of " + maxRetries + ")");
                objectListener.initializeAsServer(Constants.DEFAULT_PORT);
                initialized = true;
                System.out.println("Successfully initialized server!");
            } catch (IOException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    System.err.println("Failed to initialize network after " + maxRetries + " attempts: " + e.getMessage());
                    return;
                }
                System.out.println("Initialization attempt failed. Retrying in 1 second...");
                try {
                    Thread.sleep(1000); // Wait 1 second before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println("Retry interrupted: " + ie.getMessage());
                    return;
                }
            }
        }

        // Add shutdown hook to clean up resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server shutting down...");
            objectListener.stop();
        }));

        objectListener.start();

        // Test connection and retrieve object with status

        boolean objectListenerInitialized = false;
        int objectListenerConnectionAttempt = 0;
        Position mockConnectionTest = new Position();
        ObjectListener.DestinationMapping testMapping = new ObjectListener.DestinationMapping("testReconnectionDataThatIsReallyBadAndReallyShouldThinkOfABetterWay");
        objectListener.put(testMapping, mockConnectionTest);

        System.out.println("Testing object stuff");
        try {
            while (!objectListenerInitialized) {
                // Update the position to trigger a network send
                mockConnectionTest.x = objectListenerConnectionAttempt;
                objectListenerConnectionAttempt++;

                // Wait a bit for the network operation to complete
                Thread.sleep(1000);

                // Retrieve the object with its connection status
                ObjectListener.ObjectWithStatus<Position> objectWithStatus = objectListener.getWithStatus(testMapping);

                if (objectWithStatus != null) {
                    if (!objectWithStatus.didLastAttemptFail()) {
                        // Connection successful
                        System.out.println("Object listener initialized successfully - data synced");
                        objectListenerInitialized = true;
                    } else {
                        // Connection failed
                        System.out.println("Connection attempt " + objectListenerConnectionAttempt + " failed. Retrying...");
                    }
                }

                if (objectListenerConnectionAttempt > 30) {
                    System.err.println("Failed to initialize object listener after 30 seconds");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Position position = Position.getInstance();

        objectListener.put(
                new ObjectListener.DestinationMapping("position"),
                position
        );


        int i = 0;
        while (true) {
            i++;
            System.out.println("server: still running, position: " + position);
            try {
                if (i < 10) {
                    System.out.println("server: updating position to " + i);
                    position.x = i;
                } else {
                    break;
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("server interrupted, exiting.");
                break;
            }
        }
        Utils.setupClosing("server");
    }
}
