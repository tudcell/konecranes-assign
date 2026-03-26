package com.example.konecranes.application;

import com.example.konecranes.application.port.in.VehicleControlUseCase;
import com.example.konecranes.application.port.out.VehicleCommandGatewayPort;
import com.example.konecranes.messaging.ControlCommand;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Application service that maps manual control use cases to gateway commands.
 */
@Service
public class VehicleControlService implements VehicleControlUseCase {

    private final VehicleCommandGatewayPort commandGatewayPort;

    public VehicleControlService(VehicleCommandGatewayPort commandGatewayPort) {
        this.commandGatewayPort = commandGatewayPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void overrideDirection(String vehicleId, double directionDeg) throws IOException {
        ControlCommand command = new ControlCommand();
        command.setVehicleId(vehicleId);
        command.setOverrideDirectionDeg(directionDeg);
        command.setManualOverride(true);
        commandGatewayPort.sendControlCommand(vehicleId, command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void overrideSpeed(String vehicleId, double speed) throws IOException {
        ControlCommand command = new ControlCommand();
        command.setVehicleId(vehicleId);
        command.setOverrideSpeed(speed);
        command.setManualOverride(true);
        commandGatewayPort.sendControlCommand(vehicleId, command);
    }
}
