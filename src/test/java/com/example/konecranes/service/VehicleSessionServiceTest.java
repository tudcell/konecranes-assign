package com.example.konecranes.service;

import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.RegisterVehicleAck;
import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import com.example.konecranes.service.port.out.VehicleGatewayPort;
import com.example.konecranes.service.port.out.VehicleStateStore;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VehicleSessionServiceTest {

    @Test
    void registerPersistsStateAndSendsHandshake() throws IOException {
        VehicleStateStore stateStore = mock(VehicleStateStore.class);
        VehicleGatewayPort gatewayPort = mock(VehicleGatewayPort.class);
        SimulationProperties properties = new SimulationProperties();
        properties.getWorld().setWidth(1000.0);
        properties.getWorld().setHeight(700.0);

        VehicleSessionService service = new VehicleSessionService(stateStore, gatewayPort, properties);

        RegisterVehicleRequest request = new RegisterVehicleRequest();
        request.setVehicleId("VH-REG");
        request.setInitialX(120.0);
        request.setInitialY(240.0);
        request.setInitialDirectionDeg(45.0);
        request.setInitialSpeed(60.0);
        request.setRadius(16.0);

        when(stateStore.findAllExcept("VH-REG")).thenReturn(Collections.emptyList());
        BufferedWriter writer = new BufferedWriter(new StringWriter());

        service.register(request, writer);

        ArgumentCaptor<VehicleState> stateCaptor = ArgumentCaptor.forClass(VehicleState.class);
        verify(stateStore).upsert(stateCaptor.capture());
        VehicleState persisted = stateCaptor.getValue();
        assertEquals("VH-REG", persisted.getId());
        assertEquals(VehicleStatus.ACTIVE, persisted.getStatus());
        assertTrue(persisted.getTimestamp() > 0);

        verify(gatewayPort).attach("VH-REG", writer);

        ArgumentCaptor<RegisterVehicleAck> ackCaptor = ArgumentCaptor.forClass(RegisterVehicleAck.class);
        verify(gatewayPort).sendAck(org.mockito.ArgumentMatchers.eq("VH-REG"), ackCaptor.capture());
        assertEquals(1000.0, ackCaptor.getValue().getWorld().getWidth());
        assertEquals(700.0, ackCaptor.getValue().getWorld().getHeight());

        ArgumentCaptor<EnvironmentUpdate> envCaptor = ArgumentCaptor.forClass(EnvironmentUpdate.class);
        verify(gatewayPort).sendEnvironment(org.mockito.ArgumentMatchers.eq("VH-REG"), envCaptor.capture());
        assertSame(Collections.emptyList(), envCaptor.getValue().getNearbyVehicles());
    }
}


