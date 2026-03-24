package com.example.konecranes.application.port.in;

public class DisconnectVehicleSessionCommand {
    private final String vehicleId;

    public DisconnectVehicleSessionCommand(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleId() {
        return vehicleId;
    }
}


