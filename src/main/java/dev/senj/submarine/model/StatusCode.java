package dev.senj.submarine.model;

/**
 * Defines the possible health status of a component. *
 */
public enum StatusCode {
    OK,         // Component is operating nominally
    WARNING,    // Component is operating but reports a non-critical issue
    ERROR       // Component has failed or is non-responsive
}
