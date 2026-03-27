package com.example.konecranes.application;

import com.example.konecranes.application.port.in.SimulationQueryUseCase;
import com.example.konecranes.application.port.out.SimulationSnapshotPublisher;
import com.example.konecranes.model.SimulationSnapshot;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SimulationSchedulerTest {

    @Test
    void broadcastEnvironmentDelegatesToBroadcastService() {
        EnvironmentBroadcastService environmentBroadcastService = mock(EnvironmentBroadcastService.class);
        SimulationSnapshotPublisher simulationSnapshotPublisher = mock(SimulationSnapshotPublisher.class);
        SimulationQueryUseCase simulationQueryUseCase = mock(SimulationQueryUseCase.class);

        SimulationScheduler scheduler = new SimulationScheduler(
                environmentBroadcastService,
                simulationSnapshotPublisher,
                simulationQueryUseCase
        );

        scheduler.broadcastEnvironment();

        verify(environmentBroadcastService).broadcastToAll();
        verifyNoInteractions(simulationSnapshotPublisher, simulationQueryUseCase);
    }

    @Test
    void publishSnapshotGetsCurrentSnapshotAndPublishesIt() {
        EnvironmentBroadcastService environmentBroadcastService = mock(EnvironmentBroadcastService.class);
        SimulationSnapshotPublisher simulationSnapshotPublisher = mock(SimulationSnapshotPublisher.class);
        SimulationQueryUseCase simulationQueryUseCase = mock(SimulationQueryUseCase.class);

        SimulationSnapshot snapshot = new SimulationSnapshot();
        when(simulationQueryUseCase.currentSnapshot()).thenReturn(snapshot);

        SimulationScheduler scheduler = new SimulationScheduler(
                environmentBroadcastService,
                simulationSnapshotPublisher,
                simulationQueryUseCase
        );

        scheduler.publishSnapshot();

        verify(simulationQueryUseCase).currentSnapshot();
        verify(simulationSnapshotPublisher).publish(snapshot);
        verifyNoInteractions(environmentBroadcastService);
    }
}