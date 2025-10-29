package dev.senj.common;

import java.util.Random;

public class Constants {
    public static final String GAME_NAME = "BOIDS";

    // Network constants
    public static final int DEFAULT_PORT = new Random(System.currentTimeMillis() / 1000).nextInt(65535 - 1024) + 1024;
    public static final String DEFAULT_HOST = "localhost";
}
