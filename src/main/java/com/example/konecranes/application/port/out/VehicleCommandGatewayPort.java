package com.example.konecranes.application.port.out;

import com.example.konecranes.messaging.ControlCommand;

import java.io.IOException;

/**
 * Outbound port for sending manual control commands to vehicle processes.
 *
 * Used by the application layer when a user overrides
 * vehicle direction or speed.
 */
public interface VehicleCommandGatewayPort {

    /**
     * Sends one control command to a vehicle process.
     *
     * @param vehicleId destination vehicle id
     * @param command manual control command
     * @throws IOException when transport delivery fails
     */
    void sendControlCommand(String vehicleId, ControlCommand command) throws IOException;
}