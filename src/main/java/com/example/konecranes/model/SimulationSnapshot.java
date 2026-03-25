package com.example.konecranes.model;

import java.util.List;

/**
 * Read-model payload representing one rendered simulation frame.
 */
public class SimulationSnapshot {
    private long generatedAt;
    private SimulationWorld world;
    private List<VehicleState> vehicles;
    private int collisionWarnings;

    /** @return snapshot generation time in epoch milliseconds */
    public long getGeneratedAt() {
        return this.generatedAt;
    }

    /** @param generatedAt snapshot generation time in epoch milliseconds */
    public void setGeneratedAt(long generatedAt) {
        this.generatedAt = generatedAt;
    }

    /** @return world dimensions captured for this snapshot */
    public SimulationWorld getWorld() {
        return this.world;
    }

    /** @param world world dimensions captured for this snapshot */
    public void setWorld(SimulationWorld world) {
        this.world = world;
    }

    /** @return vehicle states visible in this snapshot */
    public List<VehicleState> getVehicles() {
        return this.vehicles;
    }

    /** @param vehicles vehicle states visible in this snapshot */
    public void setVehicles(List<VehicleState> vehicles) {
        this.vehicles = vehicles;
    }

    /** @return number of high-risk vehicles in the snapshot */
    public int getCollisionWarnings() {
        return this.collisionWarnings;
    }

    /** @param collisionWarnings number of high-risk vehicles in the snapshot */
    public void setCollisionWarnings(int collisionWarnings) {
        this.collisionWarnings = collisionWarnings;
    }
}
