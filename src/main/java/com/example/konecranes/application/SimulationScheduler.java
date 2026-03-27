package com.example.konecranes.application;

import com.example.konecranes.application.port.in.SimulationQueryUseCase;
import com.example.konecranes.application.port.out.SimulationSnapshotPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler that drives periodic simulation tasks.
 *
 * Responsible for:
 * - broadcasting environment updates to vehicles
 * - publishing simulation snapshots to subscribers
 */
@Component
public class SimulationScheduler {

    private final EnvironmentBroadcastService environmentBroadcastService;
    private final SimulationSnapshotPublisher simulationSnapshotPublisher;
    private final SimulationQueryUseCase simulationQueryUseCase;

    public SimulationScheduler(EnvironmentBroadcastService environmentBroadcastService,
                               SimulationSnapshotPublisher simulationSnapshotPublisher,
                               SimulationQueryUseCase simulationQueryUseCase) {
        this.environmentBroadcastService = environmentBroadcastService;
        this.simulationSnapshotPublisher = simulationSnapshotPublisher;
        this.simulationQueryUseCase = simulationQueryUseCase;
    }

    /**
     * Broadcasts environment updates to all active vehicles.
     *
     * Runs periodically using the configured scheduler delay.
     */
    @Scheduled(fixedDelayString = "${simulation.scheduler.fixedDelayMillis:150}")
    public void broadcastEnvironment() {
        environmentBroadcastService.broadcastToAll();
    }

    /**
     * Publishes the latest simulation snapshot to subscribers.
     *
     * Runs periodically using the configured scheduler delay.
     */
    @Scheduled(fixedDelayString = "${simulation.scheduler.fixedDelayMillis:150}")
    public void publishSnapshot() {
        simulationSnapshotPublisher.publish(simulationQueryUseCase.currentSnapshot());
    }
}