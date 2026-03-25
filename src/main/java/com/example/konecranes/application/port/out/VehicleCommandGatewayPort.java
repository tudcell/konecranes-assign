package com.example.konecranes.application.port.out;

import com.example.konecranes.messaging.ControlCommand;

import java.io.IOException;

/**
 * Outbound gateway port for manual control commands.
 */
public interface VehicleCommandGatewayPort {

    /**
     * Sends one control command to a vehicle process.
     *
     * @param vehicleId destination vehicle id
     * @param command direction/speed override payload
     * @throws IOException when transport write fails
     */
    void sendControlCommand(String vehicleId, ControlCommand command) throws IOException;
}
