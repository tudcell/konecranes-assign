package com.example.konecranes.application.port.out;

import com.example.konecranes.messaging.RegisterVehicleAck;

import java.io.IOException;

/**
 * Outbound gateway port for registration acknowledgements.
 */
public interface VehicleRegistrationGatewayPort {

    /**
     * Sends the registration acknowledgement to one vehicle process.
     *
     * @param vehicleId destination vehicle id
     * @param ack payload containing world and session data
     * @throws IOException when transport write fails
     */
    void sendAck(String vehicleId, RegisterVehicleAck ack) throws IOException;
}
