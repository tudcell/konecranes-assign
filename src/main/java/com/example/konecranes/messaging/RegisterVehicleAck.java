package com.example.konecranes.messaging;

import com.example.konecranes.model.SimulationWorld;

/**
 * Registration acknowledgement returned by coordinator to vehicle process.
 */
public class RegisterVehicleAck {
    private String vehicleId;
    private SimulationWorld world;

    /** @return acknowledged vehicle id */
    public String getVehicleId() {
        return vehicleId;
    }

    /** @param vehicleId acknowledged vehicle id */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /** @return simulation world dimensions */
    public SimulationWorld getWorld() {
        return world;
    }

    /** @param world simulation world dimensions */
    public void setWorld(SimulationWorld world) {
        this.world = world;
    }
}
