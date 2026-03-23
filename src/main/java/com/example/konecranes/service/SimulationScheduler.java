package com.example.konecranes.service;

import com.example.konecranes.service.port.in.SimulationQueryUseCase;
import com.example.konecranes.service.port.out.SimulationSnapshotPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SimulationScheduler {

    private final EnvironmentBroadcastService environmentBroadcastService;
    private final SimulationSnapshotPublisher snapshotPublisher;
    private final SimulationQueryUseCase simulationQueryUseCase;

    public SimulationScheduler(EnvironmentBroadcastService environmentBroadcastService,
                               SimulationSnapshotPublisher snapshotPublisher,
                               SimulationQueryUseCase simulationQueryUseCase) {
        this.environmentBroadcastService = environmentBroadcastService;
        this.snapshotPublisher = snapshotPublisher;
        this.simulationQueryUseCase = simulationQueryUseCase;
    }

    @Scheduled(fixedDelayString = "${simulation.scheduler.fixedDelayMillis:150}")
    public void broadcastEnvironment() {
        environmentBroadcastService.broadcastToAll();
    }

    @Scheduled(fixedDelayString = "${simulation.scheduler.fixedDelayMillis:150}")
    public void publishSnapshot() {
        snapshotPublisher.publish(simulationQueryUseCase.currentSnapshot());
    }
}
