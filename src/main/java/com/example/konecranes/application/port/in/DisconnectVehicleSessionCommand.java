package com.example.konecranes.application.port.in;

import lombok.Getter;

/**
 * Command object representing one disconnected vehicle session.
 *
 * Carries the vehicle id needed by the application layer
 * to mark the session as disconnected.
 */
@Getter
public class DisconnectVehicleSessionCommand {

    private final String vehicleId;

    /**
     * Creates a disconnect command for one vehicle session.
     *
     * @param vehicleId unique vehicle identifier
     */
    public DisconnectVehicleSessionCommand(String vehicleId) {
        this.vehicleId = vehicleId;
    }
}