package com.example.konecranes.application.port.in;

/**
 * Command that identifies which vehicle session disconnected.
 */
public class DisconnectVehicleSessionCommand {
    private final String vehicleId;

    /**
     * @param vehicleId unique vehicle identifier
     */
    public DisconnectVehicleSessionCommand(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * @return disconnected vehicle id
     */
    public String getVehicleId() {
        return vehicleId;
    }
}
