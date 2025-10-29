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

        // Initialize the network as a client
        try {
            objectListener.initializeAsClient(Constants.DEFAULT_HOST, Constants.DEFAULT_PORT);
        } catch (IOException e) {
            System.err.println("Failed to initialize network: " + e.getMessage());
            return;
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
                new ObjectListener.DestinationMapping(
                        1, 0
                ),
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
