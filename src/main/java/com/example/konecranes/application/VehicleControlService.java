package com.example.konecranes.application;

import com.example.konecranes.application.port.in.VehicleControlUseCase;
import com.example.konecranes.application.port.out.VehicleCommandGatewayPort;
import com.example.konecranes.messaging.ControlCommand;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Application service that translates manual control actions
 * into outbound vehicle control commands.
 */
@Service
public class VehicleControlService implements VehicleControlUseCase {

    private final VehicleCommandGatewayPort vehicleCommandGatewayPort;

    public VehicleControlService(VehicleCommandGatewayPort vehicleCommandGatewayPort) {
        this.vehicleCommandGatewayPort = vehicleCommandGatewayPort;
    }

    /**
     * Sends a manual direction override to one vehicle.
     *
     * @param vehicleId target vehicle id
     * @param directionDeg desired direction in degrees
     * @throws IOException when command delivery fails
     */
    @Override
    public void overrideDirection(String vehicleId, double directionDeg) throws IOException {
        ControlCommand command = new ControlCommand();
        command.setVehicleId(vehicleId);
        command.setOverrideDirectionDeg(directionDeg);
        command.setManualOverride(true);

        vehicleCommandGatewayPort.sendControlCommand(vehicleId, command);
    }

    /**
     * Sends a manual speed override to one vehicle.
     *
     * @param vehicleId target vehicle id
     * @param speed desired speed
     * @throws IOException when command delivery fails
     */
    @Override
    public void overrideSpeed(String vehicleId, double speed) throws IOException {
        ControlCommand command = new ControlCommand();
        command.setVehicleId(vehicleId);
        command.setOverrideSpeed(speed);
        command.setManualOverride(true);

        vehicleCommandGatewayPort.sendControlCommand(vehicleId, command);
    }
}