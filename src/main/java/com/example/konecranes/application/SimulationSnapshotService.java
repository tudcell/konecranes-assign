package com.example.konecranes.application;

import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.model.SimulationWorld;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import com.example.konecranes.application.port.in.SimulationQueryUseCase;
import com.example.konecranes.application.port.out.VehicleStateStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulationSnapshotService implements SimulationQueryUseCase {

    private final VehicleStateStore vehicleStateStore;
    private final SimulationProperties properties;

    public SimulationSnapshotService(VehicleStateStore vehicleStateStore, SimulationProperties properties) {
        this.vehicleStateStore = vehicleStateStore;
        this.properties = properties;
    }

    @Override
    public SimulationSnapshot currentSnapshot() {
        List<VehicleState> vehicles = vehicleStateStore.findAll().stream()
                .filter(vehicle -> vehicle.getStatus() == VehicleStatus.ACTIVE)
                .collect(java.util.stream.Collectors.toList());
        SimulationSnapshot snapshot = new SimulationSnapshot();
        snapshot.setGeneratedAt(System.currentTimeMillis());
        snapshot.setVehicles(vehicles);
        snapshot.setWorld(new SimulationWorld(properties.getWorld().getWidth(), properties.getWorld().getHeight()));
        snapshot.setCollisionWarnings((int) vehicles.stream().filter(v -> v.getRiskLevel() == RiskLevel.HIGH).count());
        return snapshot;
    }
}


