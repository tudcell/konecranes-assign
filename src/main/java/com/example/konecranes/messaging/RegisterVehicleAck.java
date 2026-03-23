package com.example.konecranes.messaging;

import com.example.konecranes.model.SimulationWorld;

public class RegisterVehicleAck {
    private String vehicleId;
    private SimulationWorld world;

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public SimulationWorld getWorld() {
        return world;
    }

    public void setWorld(SimulationWorld world) {
        this.world = world;
    }
}
