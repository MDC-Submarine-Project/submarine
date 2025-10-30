package dev.senj.common;

public class Utils {
    public static String greeting(String name) {
        if (name == null || name.isBlank()) {
            return "Hello from common!";
        }
        return "Hello, " + name + " from common!!!!!";
    }

    public static void setupClosing(String path) {
        System.out.println("Closing " + path + "...");
        System.exit(0);
    }
}
