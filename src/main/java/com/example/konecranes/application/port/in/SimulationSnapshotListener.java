package com.example.konecranes.application.port.in;

import com.example.konecranes.model.SimulationSnapshot;

/**
 * Listener for simulation snapshot events.
 *
 * Used by the streaming use case to deliver
 * snapshot updates to subscribers.
 */
@FunctionalInterface
public interface SimulationSnapshotListener {

    /**
     * Handles one simulation snapshot event.
     *
     * @param snapshot latest simulation snapshot
     * @throws Exception when the listener cannot process the snapshot
     */
    void onSnapshot(SimulationSnapshot snapshot) throws Exception;
}