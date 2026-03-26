package com.example.konecranes.application;

import com.example.konecranes.application.port.in.RegisterVehicleSessionCommand;
import com.example.konecranes.application.port.out.VehicleEnvironmentGatewayPort;
import com.example.konecranes.application.port.out.VehicleRegistrationGatewayPort;
import com.example.konecranes.application.port.out.VehicleStateRepository;
import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.RegisterVehicleAck;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
        VehicleStateRepository stateStore = mock(VehicleStateRepository.class);
        VehicleRegistrationGatewayPort registrationGatewayPort = mock(VehicleRegistrationGatewayPort.class);
        VehicleEnvironmentGatewayPort environmentGatewayPort = mock(VehicleEnvironmentGatewayPort.class);
        SimulationProperties properties = new SimulationProperties();
        properties.getWorld().setWidth(1000.0);
        properties.getWorld().setHeight(700.0);

        VehicleSessionService service = new VehicleSessionService(
                stateStore,
                registrationGatewayPort,
                environmentGatewayPort,
                properties);

        RegisterVehicleSessionCommand command = new RegisterVehicleSessionCommand(
                "VH-REG",
                120.0,
                240.0,
                45.0,
                60.0,
                16.0);

        when(stateStore.findAllExcept("VH-REG")).thenReturn(Collections.emptyList());
        service.register(command);

        ArgumentCaptor<VehicleState> stateCaptor = ArgumentCaptor.forClass(VehicleState.class);
        verify(stateStore).upsert(stateCaptor.capture());
        VehicleState persisted = stateCaptor.getValue();
        assertEquals("VH-REG", persisted.getId());
        assertEquals(VehicleStatus.ACTIVE, persisted.getStatus());
        assertTrue(persisted.getTimestamp() > 0);

        ArgumentCaptor<RegisterVehicleAck> ackCaptor = ArgumentCaptor.forClass(RegisterVehicleAck.class);
        verify(registrationGatewayPort).sendAck(org.mockito.ArgumentMatchers.eq("VH-REG"), ackCaptor.capture());
        assertEquals(1000.0, ackCaptor.getValue().getWorld().getWidth());
        assertEquals(700.0, ackCaptor.getValue().getWorld().getHeight());

        ArgumentCaptor<EnvironmentUpdate> envCaptor = ArgumentCaptor.forClass(EnvironmentUpdate.class);
        verify(environmentGatewayPort).sendEnvironment(org.mockito.ArgumentMatchers.eq("VH-REG"), envCaptor.capture());
        assertSame(Collections.emptyList(), envCaptor.getValue().getNearbyVehicles());
    }
}

