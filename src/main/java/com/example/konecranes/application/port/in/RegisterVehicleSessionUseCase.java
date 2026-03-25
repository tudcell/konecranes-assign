package com.example.konecranes.application.port.in;

import java.io.IOException;

/**
 * Handles the initial registration handshake for a vehicle session.
 */
public interface RegisterVehicleSessionUseCase {

    /**
     * Registers a newly connected vehicle and sends initial session data.
     *
     * @param command registration payload from transport layer
     * @throws IOException when handshake replies cannot be sent
     */
    void register(RegisterVehicleSessionCommand command) throws IOException;
}
