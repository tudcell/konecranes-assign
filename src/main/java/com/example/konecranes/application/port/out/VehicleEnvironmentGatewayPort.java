package com.example.konecranes.application.port.out;

import com.example.konecranes.messaging.EnvironmentUpdate;

import java.io.IOException;

/**
 * Outbound port for sending environment updates to vehicle processes.
 *
 * Used by the application layer to provide each vehicle
 * with the latest nearby-traffic context.
 */
public interface VehicleEnvironmentGatewayPort {

    /**
     * Sends one environment update to a vehicle process.
     *
     * @param vehicleId destination vehicle id
     * @param update nearby-vehicle environment snapshot
     * @throws IOException when transport delivery fails
     */
    void sendEnvironment(String vehicleId, EnvironmentUpdate update) throws IOException;
}