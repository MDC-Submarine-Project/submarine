package dev.senj.server;

import dev.senj.common.ObjectListener;
import dev.senj.common.Position;
import dev.senj.common.Utils;

public class ServerMain {
    public static void main(String[] args) {
        ObjectListener objectListener = ObjectListener.getInstance();

        objectListener.start();

        Position position = Position.getInstance();

        objectListener.put(
                new ObjectListener.DestinationMapping(
                        0,1
                ),
                position
        );


        int i = 0;
        while (true) {
            i++;
            System.out.println("server: still running");
            try {
                if (i < 10) {
                    System.out.println("server: updating position to " + i);
                    position.x = i;
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("server interrupted, exiting.");
                break;
            }
        }
    }
}
