package com.example.konecranes.application.port.in;

/**
 * Handles vehicle session termination events.
 */
public interface DisconnectVehicleSessionUseCase {

    /**
     * Marks the vehicle session as disconnected.
     *
     * @param command payload containing the vehicle identifier
     */
    void disconnect(DisconnectVehicleSessionCommand command);
}
