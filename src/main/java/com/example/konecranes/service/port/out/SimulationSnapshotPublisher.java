package com.example.konecranes.service.port.out;

import com.example.konecranes.model.SimulationSnapshot;

public interface SimulationSnapshotPublisher {
    void publish(SimulationSnapshot snapshot);
}

