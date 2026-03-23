package com.example.konecranes.messaging;

import com.example.konecranes.model.VehicleState;

import java.util.List;

public class EnvironmentUpdate {
    private List<VehicleState> nearbyVehicles;
    private long timestamp;

    public List<VehicleState> getNearbyVehicles() {
        return nearbyVehicles;
    }

    public void setNearbyVehicles(List<VehicleState> nearbyVehicles) {
        this.nearbyVehicles = nearbyVehicles;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
