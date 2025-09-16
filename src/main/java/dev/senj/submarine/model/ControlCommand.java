package dev.senj.submarine.model;

/**
 * Represents a standardized command sent to the ROV.
 * @param type The type of action to perform
 * @param value The magnitude of the action (e.g., thrust percentage)
 */
public record ControlCommand(
        CommandType type,
        double value
) {}
