package dev.senj.submarine.model;

/**
 * Placeholder representing a single frame of video data from the ROV's camera.
 * @param frameData
 * @param timestamp
 */

public record VideoFrame(byte[] frameData, long timestamp) {}
