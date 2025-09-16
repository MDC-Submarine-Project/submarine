package dev.senj.submarine.model;

/**
 * Represents a single snapshot of the ROV's telemetry data.
 * @param batteryLevel Remaining battery percentage
 * @param depthMeters Current depth below the surface
 * @param headingDegrees Compass heading
 * @param waterTemperatures Water temperature in Fahrenheit
 */

public record TelemetryData(
    double batteryLevel,
    double depthMeters,
    double headingDegrees,
    double waterTemperatures
) {}
