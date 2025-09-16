package dev.senj.submarine.preparation;

import dev.senj.submarine.model.SystemStatus;
import java.io.File;
import java.util.List;

/**
 * Defines the contract for pre-mission setup and system diagnostics.
 */
public interface MissionPreparation {
    List<SystemStatus> runPreMissionDiagnostics();
    boolean loadMissionPlan(File missionFile);
}
