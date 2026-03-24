package com.example.konecranes.application.port.in;

import com.example.konecranes.model.SimulationSnapshot;

@FunctionalInterface
public interface SimulationSnapshotListener {
    void onSnapshot(SimulationSnapshot snapshot) throws Exception;
}


