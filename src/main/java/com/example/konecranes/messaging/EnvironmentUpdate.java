package com.example.konecranes.messaging;

import com.example.konecranes.model.VehicleState;

import java.util.List;

/**
 * Environment snapshot sent by coordinator to each vehicle process.
 */
public class EnvironmentUpdate {
    private List<VehicleState> nearbyVehicles;
    private long timestamp;

    /** @return nearby vehicle states used for AI decisions */
    public List<VehicleState> getNearbyVehicles() {
        return nearbyVehicles;
    }

    /** @param nearbyVehicles nearby vehicle states used for AI decisions */
    public void setNearbyVehicles(List<VehicleState> nearbyVehicles) {
        this.nearbyVehicles = nearbyVehicles;
    }

    /** @return snapshot timestamp in epoch milliseconds */
    public long getTimestamp() {
        return timestamp;
    }

    /** @param timestamp snapshot timestamp in epoch milliseconds */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
