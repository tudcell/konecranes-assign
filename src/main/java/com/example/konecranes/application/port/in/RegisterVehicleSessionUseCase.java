package com.example.konecranes.application.port.in;

import java.io.IOException;

/**
 * Use case for registering a newly connected vehicle session.
 *
 * Handles the initial registration flow after a vehicle
 * establishes a connection with the coordinator.
 */
public interface RegisterVehicleSessionUseCase {

    /**
     * Registers a new vehicle session and completes the initial handshake.
     *
     * Typical responsibilities include:
     * - storing the initial vehicle state
     * - sending registration acknowledgement
     * - sending initial environment data
     *
     * @param command registration input from the transport layer
     * @throws IOException when handshake responses cannot be sent
     */
    void register(RegisterVehicleSessionCommand command) throws IOException;
}