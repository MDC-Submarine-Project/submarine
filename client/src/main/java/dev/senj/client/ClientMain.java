package dev.senj.client;

import dev.senj.common.Utils;

public class ClientMain {
    public static void main(String[] args) {
        System.out.println(Utils.greeting("Client"));
        while (true) {
            System.out.println("client: still running");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("client interrupted, exiting.");
                break;
            }
        }
    }
}
