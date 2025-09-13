package dev.senj.submarine.api;

/**
 * Represents a single mesh point within the chunk
 */
public interface MeshPoint {
    /**
     * @return X offset from chunk origin in centimeters
     */
    int getChunkOffsetX();

    /**
     * @return Y offset from chunk origin in centimeters
     */
    int getChunkOffsetY();

    /**
     * @return Z offset from chunk origin in centimeters
     */
    int getChunkOffsetZ();

    /**
     * @return X coordinate in centimeters
     */
    double getX();

    /**
     * @return Y coordinate in centimeters
     */
    double getY();

    /**
     * @return Z coordinate in centimeters
     */
    double getZ();
}