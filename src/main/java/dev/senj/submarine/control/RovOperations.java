package dev.senj.submarine.control;

import dev.senj.submarine.model.*;

/**
 * Defines the operational methods for the ROV pilot.
 */
public interface RovOperations {
    /**
     * Initiates and validates a secure connection to the ROV.
     * This must be successful before other control methods are enabled.
     * @return true if the secure link is established and stable, false otherwise.
     */
    boolean establishSecureLink();

    /**
     * Translated a raw user input event (e.g., key press, joystick movement)
     * into a standardized command for the ROV.
     * @param inputEvent The raw input data from the Pilot's controller.
     */
    void processPilotInput(InputEvent inputEvent);

    /**
     * Receives and parses the latest telemetry data packed from the ROV.
     * @return A data object containing current battery, depth, heading, etc.
     */
    TelemetryData receiveTelemetryData();

    /**
     * Receives and decodes the latest video frame from the ROV's camera.
     * @return A renderable image or video fram for display.
     */
    VideoFrame receiveVideoFrame();

    /**
     * Checks the status of the connection heartbeat signal from the ROV.
     * This method is the basis for triggering connection loss alerts.
     * @return The current status of the connection (e.g., CONNECTED, UNSTABLE, LOST).
     */
    ConnectionStatus checkConnectionStatus();

    /**
     * Analyzes the latest telemetry data for any values that cross critical
     * thresholds, such as low battery.
     * @param currentTelemetry The telemetry data to analyze.
     * @return An Alert object if a threshold is crossed, null otherwise.
     */
    SystemAlert checkForSystemAlerts(TelemetryData currentTelemetry);
}
