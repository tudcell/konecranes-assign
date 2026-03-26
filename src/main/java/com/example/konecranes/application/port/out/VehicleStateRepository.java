package com.example.konecranes.application.port.out;

import com.example.konecranes.model.VehicleState;

import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence port for vehicle states.
 */
public interface VehicleStateRepository {

    /**
     * Inserts or updates one vehicle state.
     *
     * @param state latest state to persist
     */
    void upsert(VehicleState state);

    /**
     * Finds one state by vehicle id.
     *
     * @param vehicleId vehicle identifier
     * @return stored state if present
     */
    Optional<VehicleState> findById(String vehicleId);

    /**
     * Returns all known vehicle states.
     *
     * @return snapshot of all vehicles
     */
    List<VehicleState> findAll();

    /**
     * Returns all states except one vehicle.
     *
     * @param vehicleId vehicle to exclude
     * @return snapshot of all other vehicles
     */
    List<VehicleState> findAllExcept(String vehicleId);

    /**
     * Removes one vehicle state from store.
     *
     * @param vehicleId vehicle identifier
     */
    void remove(String vehicleId);
}
