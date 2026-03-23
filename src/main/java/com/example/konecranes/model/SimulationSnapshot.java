package com.example.konecranes.model;

import java.util.List;

public class SimulationSnapshot {
    private long generatedAt;
    private SimulationWorld world;
    private List<VehicleState> vehicles;
    private int collisionWarnings;

    public long getGeneratedAt() {
        return this.generatedAt;
    }

    public void setGeneratedAt(long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public SimulationWorld getWorld() {
        return this.world;
    }

    public void setWorld(SimulationWorld world) {
        this.world = world;
    }

    public List<VehicleState> getVehicles() {
        return this.vehicles;
    }

    public void setVehicles(List<VehicleState> vehicles) {
        this.vehicles = vehicles;
    }

    public int getCollisionWarnings() {
        return this.collisionWarnings;
    }

    public void setCollisionWarnings(int collisionWarnings) {
        this.collisionWarnings = collisionWarnings;
    }
}
