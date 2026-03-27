package com.example.konecranes.adapter.out.persistence;

import com.example.konecranes.application.port.out.VehicleStateRepository;
import com.example.konecranes.model.VehicleState;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for vehicle states.
 *
 * Stores the latest known state for each vehicle.
 * Returns defensive copies so callers cannot mutate
 * the repository's internal state accidentally.
 */
@Repository
public class InMemoryVehicleStateRepository implements VehicleStateRepository {

    private final Map<String, VehicleState> vehicleStates = new ConcurrentHashMap<>();

    /**
     * Inserts or replaces the state for one vehicle.
     *
     * A defensive copy is stored to prevent external mutation
     * after the state has been written to the repository.
     *
     * @param state vehicle state to insert or update
     */
    @Override
    public void upsert(VehicleState state) {
        vehicleStates.put(state.getId(), state.copy());
    }

    /**
     * Finds one vehicle by id.
     *
     * Returns a defensive copy when the vehicle exists.
     *
     * @param vehicleId target vehicle id
     * @return optional containing a copied state when found
     */
    @Override
    public Optional<VehicleState> findById(String vehicleId) {
        VehicleState state = vehicleStates.get(vehicleId);
        return Optional.ofNullable(state == null ? null : state.copy());
    }

    /**
     * Returns all stored vehicle states.
     *
     * Each returned element is a defensive copy.
     *
     * @return snapshot list of all vehicle states
     */
    @Override
    public List<VehicleState> findAll() {
        List<VehicleState> snapshot = new ArrayList<>();
        for (VehicleState vehicle : vehicleStates.values()) {
            snapshot.add(vehicle.copy());
        }
        return snapshot;
    }

    /**
     * Returns all stored vehicle states except the given vehicle id.
     *
     * Each returned element is a defensive copy.
     *
     * @param vehicleId vehicle id to exclude
     * @return snapshot list without the excluded vehicle
     */
    @Override
    public List<VehicleState> findAllExcept(String vehicleId) {
        List<VehicleState> snapshot = new ArrayList<>();
        for (VehicleState vehicle : vehicleStates.values()) {
            if (!vehicle.getId().equals(vehicleId)) {
                snapshot.add(vehicle.copy());
            }
        }
        return snapshot;
    }

    /**
     * Removes one vehicle state from the repository.
     *
     * @param vehicleId vehicle id to remove
     */
    @Override
    public void remove(String vehicleId) {
        vehicleStates.remove(vehicleId);
    }
}