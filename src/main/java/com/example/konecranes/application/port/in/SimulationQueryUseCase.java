package com.example.konecranes.application.port.in;

import com.example.konecranes.model.SimulationSnapshot;

/**
 * Exposes read-only simulation state queries.
 */
public interface SimulationQueryUseCase {

    /**
     * Returns the latest computed simulation snapshot.
     *
     * @return immutable view model of world and vehicle states
     */
    SimulationSnapshot currentSnapshot();
}
