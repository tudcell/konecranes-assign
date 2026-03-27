package com.example.konecranes.application.port.out;

/**
 * Application-facing registry for active vehicle session channels.
 */
public interface VehicleSessionRegistryPort {

    /**
     * Attaches one active channel to a vehicle id.
     *
     * @param vehicleId vehicle id
     * @param channel active session channel
     */
    void attach(String vehicleId, VehicleSessionChannel channel);

    /**
     * Detaches and closes the channel for one vehicle id.
     *
     * @param vehicleId vehicle id
     */
    void detach(String vehicleId);

    /**
     * Detaches and closes all active channels.
     */
    void detachAll();
}