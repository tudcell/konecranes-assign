package com.example.konecranes.application.port.in;

/**
 * Use case for handling a disconnected vehicle session.
 *
 * Defines the operation needed by the application layer
 * to mark a vehicle session as disconnected.
 */
public interface DisconnectVehicleSessionUseCase {

    /**
     * Disconnects one vehicle session.
     *
     * @param command command containing the vehicle id
     */
    void disconnect(DisconnectVehicleSessionCommand command);
}