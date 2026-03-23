package com.example.konecranes.service;

import com.example.konecranes.messaging.ControlCommand;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VehicleCommandService {

    private final VehicleConnectionManager connectionManager;

    public VehicleCommandService(VehicleConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void overrideDirection(String vehicleId, double directionDeg) throws IOException {
        ControlCommand command = new ControlCommand();
        command.setVehicleId(vehicleId);
        command.setOverrideDirectionDeg(directionDeg);
        command.setManualOverride(true);
        connectionManager.sendControlCommand(vehicleId, command);
    }

    public void overrideSpeed(String vehicleId, double speed) throws IOException {
        ControlCommand command = new ControlCommand();
        command.setVehicleId(vehicleId);
        command.setOverrideSpeed(speed);
        command.setManualOverride(true);
        connectionManager.sendControlCommand(vehicleId, command);
    }
}
