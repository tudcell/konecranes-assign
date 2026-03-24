package com.example.konecranes.application.port.in;

public interface SimulationStreamUseCase {
    String subscribe(SimulationSnapshotListener listener);

    void unsubscribe(String subscriptionId);
}


