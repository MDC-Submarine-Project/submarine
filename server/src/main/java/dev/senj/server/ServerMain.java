package dev.senj.server;

import dev.senj.common.Constants;
import dev.senj.common.ObjectListener;
import dev.senj.common.Position;
import dev.senj.common.Utils;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) {
        ObjectListener objectListener = ObjectListener.getInstance();

        // Initialize the network as a server
        try {
            objectListener.initializeAsServer(Constants.DEFAULT_PORT);
        } catch (IOException e) {
            System.err.println("Failed to initialize network: " + e.getMessage());
            return;
        }

        // Add shutdown hook to clean up resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server shutting down...");
            objectListener.stop();
        }));

        objectListener.start();

        Position position = Position.getInstance();

        objectListener.put(
                new ObjectListener.DestinationMapping(),
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
