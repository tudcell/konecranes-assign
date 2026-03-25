package com.example.konecranes.adapter.out.tcp;

import java.io.BufferedWriter;

/**
 * Registry abstraction for active vehicle TCP writers.
 */
public interface VehicleSessionConnectionRegistry {

    /**
     * Registers the writer for a connected vehicle session.
     *
     * @param vehicleId connected vehicle id
     * @param writer session writer bound to the vehicle socket
     */
    void attach(String vehicleId, BufferedWriter writer);

    /**
     * Removes one vehicle writer and closes associated session resources.
     *
     * @param vehicleId vehicle id to detach
     */
    void detach(String vehicleId);

    /**
     * Detaches and closes all active vehicle sessions.
     */
    void detachAll();
}


