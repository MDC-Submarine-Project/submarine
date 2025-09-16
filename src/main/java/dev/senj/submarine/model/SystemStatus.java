package dev.senj.submarine.model;

/**
 * Represents the operational status of a single component.
 * @param componentName The name of the component being reported on
 * @param status The health check status code
 * @param message A human-readable status message
 */
public record SystemStatus(
        String componentName,
        StatusCode status,
        String message
) {}
