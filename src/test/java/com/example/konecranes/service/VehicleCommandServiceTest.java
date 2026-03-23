package com.example.konecranes.service;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.service.port.out.VehicleGatewayPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VehicleCommandServiceTest {

    @Test
    void overrideDirectionBuildsManualControlCommand() throws IOException {
        VehicleGatewayPort gatewayPort = mock(VehicleGatewayPort.class);
        VehicleCommandService service = new VehicleCommandService(gatewayPort);

        service.overrideDirection("VH-TEST", 180.0);

        ArgumentCaptor<ControlCommand> captor = ArgumentCaptor.forClass(ControlCommand.class);
        verify(gatewayPort).sendControlCommand(org.mockito.ArgumentMatchers.eq("VH-TEST"), captor.capture());

        ControlCommand command = captor.getValue();
        assertEquals("VH-TEST", command.getVehicleId());
        assertEquals(180.0, command.getOverrideDirectionDeg());
        assertTrue(command.isManualOverride());
    }

    @Test
    void overrideSpeedBuildsManualControlCommand() throws IOException {
        VehicleGatewayPort gatewayPort = mock(VehicleGatewayPort.class);
        VehicleCommandService service = new VehicleCommandService(gatewayPort);

        service.overrideSpeed("VH-TEST", 25.0);

        ArgumentCaptor<ControlCommand> captor = ArgumentCaptor.forClass(ControlCommand.class);
        verify(gatewayPort).sendControlCommand(org.mockito.ArgumentMatchers.eq("VH-TEST"), captor.capture());

        ControlCommand command = captor.getValue();
        assertEquals("VH-TEST", command.getVehicleId());
        assertEquals(25.0, command.getOverrideSpeed());
        assertTrue(command.isManualOverride());
    }
}

