package com.example.konecranes.model;

/**
 * Lifecycle/motion status of a vehicle.
 */
public enum VehicleStatus {
    /** Vehicle is moving and participating in simulation updates. */
    ACTIVE,
    /** Vehicle is intentionally or emergently stopped. */
    STOPPED,
    /** Vehicle lost session with coordinator. */
    DISCONNECTED
}
