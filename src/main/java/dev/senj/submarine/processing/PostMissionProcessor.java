package dev.senj.submarine.processing;

import dev.senj.submarine.api.MeshChunk; // Assuming MeshChunk will be in this package
import dev.senj.submarine.model.Model3D; // TODO: New class to be made later

import java.io.File;
import java.util.List;

/**
 * Defines the contract for processing mission data after the ROV is retrieved.
 */
public interface PostMissionProcessor {
    List<MeshChunk> loadMissionData(File missionLogFile);
    Model3D generate3DModel(List<MeshChunk> missionData);
    boolean exportModelToFile(Model3d model, File destinationFile);
}
