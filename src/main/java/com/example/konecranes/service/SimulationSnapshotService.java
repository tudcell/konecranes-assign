package com.example.konecranes.service;

import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.model.SimulationWorld;
import com.example.konecranes.model.VehicleState;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulationSnapshotService {

    private final VehicleRegistry vehicleRegistry;
    private final SimulationProperties properties;

    public SimulationSnapshotService(VehicleRegistry vehicleRegistry, SimulationProperties properties) {
        this.vehicleRegistry = vehicleRegistry;
        this.properties = properties;
    }

    public SimulationSnapshot currentSnapshot() {
        List<VehicleState> vehicles = vehicleRegistry.findAll();
        SimulationSnapshot snapshot = new SimulationSnapshot();
        snapshot.setGeneratedAt(System.currentTimeMillis());
        snapshot.setVehicles(vehicles);
        snapshot.setWorld(new SimulationWorld(properties.getWorld().getWidth(), properties.getWorld().getHeight()));
        snapshot.setCollisionWarnings((int) vehicles.stream().filter(v -> v.getRiskLevel() == RiskLevel.HIGH).count());
        return snapshot;
    }
}
