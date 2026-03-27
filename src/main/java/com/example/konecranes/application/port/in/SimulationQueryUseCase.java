package com.example.konecranes.application.port.in;

import com.example.konecranes.model.SimulationSnapshot;

/**
 * Use case for reading simulation state.
 *
 * Provides read-only access to the latest snapshot
 * of the simulation world and vehicles.
 */
public interface SimulationQueryUseCase {

    /**
     * Returns the latest available simulation snapshot.
     *
     * @return current simulation snapshot
     */
    SimulationSnapshot currentSnapshot();
}