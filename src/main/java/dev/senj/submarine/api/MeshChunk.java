package dev.senj.submarine.api;

import java.util.List;

public interface MeshChunk {
    /**
     * @return The x coordinate of this chunk in the rendering context
     */
    int getChunkX();

    /**
     * @return The y coordinate of this chunk in the rendering context
     */
    int getChunkY();

    /**
     * @return The z coordinate of this chunk in the rendering context
     */
    int getChunkZ();

    /**
     * @return List of mesh points contained within this chunk
     */
    List<MeshPoint> getMeshPoints();

    /**
     * @return Size of this chunk in centimeters
     */
    default double getChunkSize() {
        return 100;
    }
}