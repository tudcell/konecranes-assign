package com.example.konecranes.service;

import com.example.konecranes.model.VehicleState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VehicleRegistry {

    private final Map<String, VehicleState> vehicles = new ConcurrentHashMap<>();

    public void upsert(VehicleState state) {
        vehicles.put(state.getId(), state.copy());
    }

    public Optional<VehicleState> findById(String vehicleId) {
        VehicleState state = vehicles.get(vehicleId);
        return Optional.ofNullable(state == null ? null : state.copy());
    }

    public List<VehicleState> findAll() {
        List<VehicleState> snapshot = new ArrayList<>();
        for (VehicleState vehicle : vehicles.values()) {
            snapshot.add(vehicle.copy());
        }
        return snapshot;
    }

    public List<VehicleState> findAllExcept(String vehicleId) {
        List<VehicleState> snapshot = new ArrayList<>();
        for (VehicleState vehicle : vehicles.values()) {
            if (!vehicle.getId().equals(vehicleId)) {
                snapshot.add(vehicle.copy());
            }
        }
        return snapshot;
    }

    public void remove(String vehicleId) {
        vehicles.remove(vehicleId);
    }
}
