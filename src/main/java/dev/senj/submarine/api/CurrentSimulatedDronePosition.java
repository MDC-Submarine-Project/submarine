package dev.senj.submarine.api;

/*
 * This contains where the drone is believed to be at the moment.
 */
public interface CurrentSimulatedDronePosition {

    int expectedX();
    int expectedY();
    int expectedZ();
}
