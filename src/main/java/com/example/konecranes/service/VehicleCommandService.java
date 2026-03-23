package com.example.konecranes.service;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.service.port.in.VehicleControlUseCase;
import com.example.konecranes.service.port.out.VehicleGatewayPort;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VehicleCommandService implements VehicleControlUseCase {

    private final VehicleGatewayPort connectionManager;

    public VehicleCommandService(VehicleGatewayPort connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void overrideDirection(String vehicleId, double directionDeg) throws IOException {
        ControlCommand command = new ControlCommand();
        command.setVehicleId(vehicleId);
        command.setOverrideDirectionDeg(directionDeg);
        command.setManualOverride(true);
        connectionManager.sendControlCommand(vehicleId, command);
    }

    @Override
    public void overrideSpeed(String vehicleId, double speed) throws IOException {
        ControlCommand command = new ControlCommand();
        command.setVehicleId(vehicleId);
        command.setOverrideSpeed(speed);
        command.setManualOverride(true);
        connectionManager.sendControlCommand(vehicleId, command);
    }
}
