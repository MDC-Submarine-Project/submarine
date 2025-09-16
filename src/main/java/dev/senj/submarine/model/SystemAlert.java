package dev.senj.submarine.model;

/**
 * Represents a system alert to be displayed to the pilot.
 * @param level e.g., level="CRITICAL"
 * @param message e.g., message="Battery below 15%"
 */
public record SystemAlert(String level,String message) {}
