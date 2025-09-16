package dev.senj.submarine.model;

/**
 * Standardized packet representing a raw input from a control device.
 * @param source e.g., source="joystick1"
 * @param action e.g., action="push_forward"
 * @param value e.g., value="0.75"
 */
public record InputEvent(String source, String action, double value) {}
