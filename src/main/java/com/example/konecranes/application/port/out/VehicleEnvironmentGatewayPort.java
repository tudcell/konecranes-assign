package com.example.konecranes.application.port.out;

import com.example.konecranes.messaging.EnvironmentUpdate;

import java.io.IOException;

/**
 * Outbound gateway port for environment update messages.
 */
public interface VehicleEnvironmentGatewayPort {

    /**
     * Sends one environment update to a vehicle process.
     *
     * @param vehicleId destination vehicle id
     * @param update snapshot of nearby vehicles
     * @throws IOException when transport write fails
     */
    void sendEnvironment(String vehicleId, EnvironmentUpdate update) throws IOException;
}
