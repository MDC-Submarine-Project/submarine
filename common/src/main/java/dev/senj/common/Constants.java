package dev.senj.common;

public class Constants {
    public static final String GAME_NAME = "BOIDS";

    // Network constants
    public static final int DEFAULT_PORT = getDefaultPort();
    public static final String DEFAULT_HOST = getDefaultHost();

    private static int getDefaultPort() {
        String portEnv = System.getenv("SUBMARINE_PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                return Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("Invalid SUBMARINE_PORT environment variable: " + portEnv);
            }
        }
        return 8080; // Default port if not specified in environment
    }

    private static String getDefaultHost() {
        String hostEnv = System.getenv("SUBMARINE_HOST");
        if (hostEnv != null && !hostEnv.isEmpty()) {
            return hostEnv;
        }
        return "localhost"; // Default host if not specified in environment
    }
}
