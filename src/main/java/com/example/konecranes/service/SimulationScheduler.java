package com.example.konecranes.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SimulationScheduler {

    private final EnvironmentBroadcastService environmentBroadcastService;
    private final SseSnapshotService sseSnapshotService;
    private final SimulationSnapshotService snapshotService;

    public SimulationScheduler(EnvironmentBroadcastService environmentBroadcastService,
                               SseSnapshotService sseSnapshotService,
                               SimulationSnapshotService snapshotService) {
        this.environmentBroadcastService = environmentBroadcastService;
        this.sseSnapshotService = sseSnapshotService;
        this.snapshotService = snapshotService;
    }

    @Scheduled(fixedDelayString = "${simulation.scheduler.fixedDelayMillis:150}")
    public void broadcastEnvironment() {
        environmentBroadcastService.broadcastToAll();
    }

    @Scheduled(fixedDelayString = "${simulation.scheduler.fixedDelayMillis:150}")
    public void publishSnapshot() {
        sseSnapshotService.publish(snapshotService.currentSnapshot());
    }
}
