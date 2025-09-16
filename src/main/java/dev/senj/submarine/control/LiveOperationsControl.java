package dev.senj.submarine.control;

import dev.senj.submarine.model.ControlCommand;
import dev.senj.submarine.model.TelemetryData;
import java.util.Optional;

/**
 * Defines the real-time command and control of the ROV.
 */

public interface LiveOperationsControl {
    /**
     * Attempts to establish a secure connection to the ROV.
     * @return true if the connection is successful, false otherwise.
     */
    boolean connectToRov();

    /**
     * Sends a specific command to the ROV.
     * @param command The command to be executed.
     */
    void sendCommand(ControlCommand command);

    /**
     * Fetches the latest available telemetry data from the ROV.
     * @return An Optional containing TelemetryData if available, otherwise empty.
     */
    Optional<TelemetryData> getLatestTelemetry();

    /**
     * Disconnects from the ROV and terminates the session.
     */
    void disconnect();
}
