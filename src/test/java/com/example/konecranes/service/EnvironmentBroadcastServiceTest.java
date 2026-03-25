package com.example.konecranes.service;

import com.example.konecranes.application.EnvironmentBroadcastService;
import com.example.konecranes.application.port.out.VehicleEnvironmentGatewayPort;
import com.example.konecranes.application.port.out.VehicleStateStore;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class EnvironmentBroadcastServiceTest {

    @Test
    void broadcastsOnlyToActiveVehiclesWithActiveNeighbors() throws IOException {
        VehicleStateStore stateStore = mock(VehicleStateStore.class);
        VehicleEnvironmentGatewayPort gatewayPort = mock(VehicleEnvironmentGatewayPort.class);

        VehicleState activeOne = state("VH-1", VehicleStatus.ACTIVE);
        VehicleState activeTwo = state("VH-2", VehicleStatus.ACTIVE);
        VehicleState disconnected = state("VH-3", VehicleStatus.DISCONNECTED);

        when(stateStore.findAll()).thenReturn(Arrays.asList(activeOne, activeTwo, disconnected));

        EnvironmentBroadcastService service = new EnvironmentBroadcastService(stateStore, gatewayPort);
        service.broadcastToAll();

        ArgumentCaptor<EnvironmentUpdate> updateCaptor = ArgumentCaptor.forClass(EnvironmentUpdate.class);
        verify(gatewayPort).sendEnvironment(eq("VH-1"), updateCaptor.capture());
        List<VehicleState> forOne = updateCaptor.getValue().getNearbyVehicles();
        assertEquals(1, forOne.size());
        assertEquals("VH-2", forOne.get(0).getId());

        verify(gatewayPort).sendEnvironment(eq("VH-2"), updateCaptor.capture());
        List<VehicleState> forTwo = updateCaptor.getValue().getNearbyVehicles();
        assertEquals(1, forTwo.size());
        assertEquals("VH-1", forTwo.get(0).getId());

        verify(gatewayPort, times(2)).sendEnvironment(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(EnvironmentUpdate.class));
        verifyNoMoreInteractions(gatewayPort);
    }

    private VehicleState state(String id, VehicleStatus status) {
        VehicleState state = new VehicleState();
        state.setId(id);
        state.setStatus(status);
        return state;
    }
}

