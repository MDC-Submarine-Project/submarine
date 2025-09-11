package dev.senj.submarine.api;

import java.util.List;

/*
 * This is the interface that the map display will use to get information from the simulation to render in the map display.
 */
public interface MapDisplayStorage {


    /**
     * @return the chunk that is centered in the map display
     */
    MeshChunk centeredChunk();
    /**
     * @return the chunks in the render range of the map display based from the centeredChunk
     */
    List<MeshChunk> chunksInRenderView();
    /**
     * @return the current simulated drone position.
     */
    CurrentSimulatedDronePosition currentSimulatedDronePosition();

}
