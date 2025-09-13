package dev.senj.submarine.api;

/*
 * This contains where the drone is believed to be at the moment.
 */
public interface CurrentSimulatedDronePosition {
    /**
     * @return The expected x coordinate of the drone position in centimeters
     */
    double expectedX();

    /**
     * @return The expected y coordinate of the drone position in centimeters
     */
    double expectedY();

    /**
     * @return The expected z coordinate of the drone position in centimeters
     */
    double expectedZ();
}
