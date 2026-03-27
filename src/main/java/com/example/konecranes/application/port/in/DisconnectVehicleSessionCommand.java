package com.example.konecranes.application.port.in;

import lombok.Getter;

/**
 * Command that identifies which vehicle session disconnected.
 */
@Getter
public class DisconnectVehicleSessionCommand {
    /**
     * vehicleId unique vehicle identifier
     */
    private final String vehicleId;
    public DisconnectVehicleSessionCommand(String vehicleId) {
        this.vehicleId = vehicleId;
    }

}
