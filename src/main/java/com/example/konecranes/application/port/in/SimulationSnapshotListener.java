package com.example.konecranes.application.port.in;

import com.example.konecranes.model.SimulationSnapshot;

/**
 * Callback for receiving simulation snapshots from the stream use case.
 */
@FunctionalInterface
public interface SimulationSnapshotListener {

    /**
     * Receives one snapshot event.
     *
     * @param snapshot latest simulation snapshot
     * @throws Exception thrown by listener implementation if delivery fails
     */
    void onSnapshot(SimulationSnapshot snapshot) throws Exception;
}
