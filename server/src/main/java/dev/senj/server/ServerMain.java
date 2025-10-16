package dev.senj.server;

import dev.senj.common.Utils;

public class ServerMain {
    public static void main(String[] args) {
        System.out.println(Utils.greeting("Server"));
        while (true) {
            System.out.println("server: still running");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("server interrupted, exiting.");
                break;
            }
        }
    }
}
