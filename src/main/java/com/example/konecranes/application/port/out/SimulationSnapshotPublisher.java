package com.example.konecranes.application.port.out;

import com.example.konecranes.model.SimulationSnapshot;

/**
 * Outbound port for publishing simulation snapshots to subscribers.
 */
public interface SimulationSnapshotPublisher {

    /**
     * Publishes one simulation snapshot event.
     *
     * @param snapshot snapshot payload to broadcast
     */
    void publish(SimulationSnapshot snapshot);
}
