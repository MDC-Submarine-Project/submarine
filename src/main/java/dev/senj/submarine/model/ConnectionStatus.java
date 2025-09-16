package dev.senj.submarine.model;

/**
 * Represents the possible states of the connection to the ROV.
 */
public enum ConnectionStatus {
    CONNECTED,
    UNSTABLE,
    LOST,
    DISCONNECTED
}
