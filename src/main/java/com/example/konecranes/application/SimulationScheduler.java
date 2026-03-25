package com.example.konecranes.application;

import com.example.konecranes.application.port.in.SimulationQueryUseCase;
import com.example.konecranes.application.port.out.SimulationSnapshotPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler that drives environment fan-out and snapshot publishing.
 */
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

    /**
     * Broadcasts environment updates to all active vehicles.
     */
    @Scheduled(fixedDelayString = "${simulation.scheduler.fixedDelayMillis:150}")
    public void broadcastEnvironment() {
        environmentBroadcastService.broadcastToAll();
    }

    /**
     * Publishes the latest snapshot to stream subscribers.
     */
    @Scheduled(fixedDelayString = "${simulation.scheduler.fixedDelayMillis:150}")
    public void publishSnapshot() {
        snapshotPublisher.publish(simulationQueryUseCase.currentSnapshot());
    }
}

