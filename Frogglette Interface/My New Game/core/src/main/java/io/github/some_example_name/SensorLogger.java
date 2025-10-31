package io.github.some_example_name;

import java.util.Random;

public class SensorLogger {

    private Random random = new Random();

    public void logSensorData() {
        double depth = 0 + random.nextDouble() * 100;      // fake depth in meters
        double temperature = 20 + random.nextDouble() * 10; // fake temperature in °C
        double speed = 0 + random.nextDouble() * 5;        // fake speed in m/s

        System.out.printf("Depth: %.2f m | Temperature: %.2f °C | Speed: %.2f m/s%n",
                depth, temperature, speed);
    }

    public static void main(String[] args) throws InterruptedException {
        SensorLogger logger = new SensorLogger();
        while (true) {
            logger.logSensorData();
            Thread.sleep(1000); // log every 1 second
        }
    }
}
