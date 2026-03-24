package com.example.konecranes.application;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.application.port.in.VehicleControlUseCase;
import com.example.konecranes.application.port.out.VehicleCommandGatewayPort;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VehicleCommandService implements VehicleControlUseCase {

    private final VehicleCommandGatewayPort commandGatewayPort;

    public VehicleCommandService(VehicleCommandGatewayPort commandGatewayPort) {
        this.commandGatewayPort = commandGatewayPort;
    }

    @Override
    public void overrideDirection(String vehicleId, double directionDeg) throws IOException {
        ControlCommand command = new ControlCommand();
        command.setVehicleId(vehicleId);
        command.setOverrideDirectionDeg(directionDeg);
        command.setManualOverride(true);
        commandGatewayPort.sendControlCommand(vehicleId, command);
    }

    @Override
    public void overrideSpeed(String vehicleId, double speed) throws IOException {
        ControlCommand command = new ControlCommand();
        command.setVehicleId(vehicleId);
        command.setOverrideSpeed(speed);
        command.setManualOverride(true);
        commandGatewayPort.sendControlCommand(vehicleId, command);
    }
}

