package com.example.konecranes.application;

import com.example.konecranes.application.port.out.VehicleCommandGatewayPort;
import com.example.konecranes.messaging.ControlCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VehicleControlServiceTest {

    @Test
    void overrideDirectionBuildsManualControlCommand() throws IOException {
        VehicleCommandGatewayPort gatewayPort = mock(VehicleCommandGatewayPort.class);
        VehicleControlService service = new VehicleControlService(gatewayPort);

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
        VehicleCommandGatewayPort gatewayPort = mock(VehicleCommandGatewayPort.class);
        VehicleControlService service = new VehicleControlService(gatewayPort);

        service.overrideSpeed("VH-TEST", 25.0);

        ArgumentCaptor<ControlCommand> captor = ArgumentCaptor.forClass(ControlCommand.class);
        verify(gatewayPort).sendControlCommand(org.mockito.ArgumentMatchers.eq("VH-TEST"), captor.capture());

        ControlCommand command = captor.getValue();
        assertEquals("VH-TEST", command.getVehicleId());
        assertEquals(25.0, command.getOverrideSpeed());
        assertTrue(command.isManualOverride());
    }
}

