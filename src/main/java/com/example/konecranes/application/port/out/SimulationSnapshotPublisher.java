package com.example.konecranes.application.port.out;

import com.example.konecranes.model.SimulationSnapshot;

public interface SimulationSnapshotPublisher {
    void publish(SimulationSnapshot snapshot);
}


