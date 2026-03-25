package com.example.konecranes.adapter.out.persistence;

import com.example.konecranes.model.VehicleState;
import com.example.konecranes.application.port.out.VehicleStateStore;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory persistence adapter for vehicle states.
 *
 * <p>All reads and writes use defensive copies to avoid shared mutable state.</p>
 */
@Repository
public class VehicleRegistry implements VehicleStateStore {

    private final Map<String, VehicleState> vehicles = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void upsert(VehicleState state) {
        vehicles.put(state.getId(), state.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<VehicleState> findById(String vehicleId) {
        VehicleState state = vehicles.get(vehicleId);
        return Optional.ofNullable(state == null ? null : state.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VehicleState> findAll() {
        List<VehicleState> snapshot = new ArrayList<>();
        for (VehicleState vehicle : vehicles.values()) {
            snapshot.add(vehicle.copy());
        }
        return snapshot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VehicleState> findAllExcept(String vehicleId) {
        List<VehicleState> snapshot = new ArrayList<>();
        for (VehicleState vehicle : vehicles.values()) {
            if (!vehicle.getId().equals(vehicleId)) {
                snapshot.add(vehicle.copy());
            }
        }
        return snapshot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String vehicleId) {
        vehicles.remove(vehicleId);
    }
}


