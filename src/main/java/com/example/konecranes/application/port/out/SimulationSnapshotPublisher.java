package com.example.konecranes.application.port.out;

import com.example.konecranes.model.SimulationSnapshot;

/**
 * Outbound port for publishing simulation snapshot events.
 *
 * Used by the application layer to deliver the latest
 * simulation snapshot to subscribed listeners or streams.
 */
public interface SimulationSnapshotPublisher {

    /**
     * Publishes one simulation snapshot.
     *
     * @param snapshot snapshot to broadcast
     */
    void publish(SimulationSnapshot snapshot);
}