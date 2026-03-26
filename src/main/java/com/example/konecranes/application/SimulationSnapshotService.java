package com.example.konecranes.application;

import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.model.SimulationWorld;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import com.example.konecranes.application.port.in.SimulationQueryUseCase;
import com.example.konecranes.application.port.out.VehicleStateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application query service that builds the read model for UI/API snapshots.
 */
@Service
public class SimulationSnapshotService implements SimulationQueryUseCase {

    private final VehicleStateRepository vehicleStateRepository;
    private final SimulationProperties properties;

    public SimulationSnapshotService(VehicleStateRepository vehicleStateRepository, SimulationProperties properties) {
        this.vehicleStateRepository = vehicleStateRepository;
        this.properties = properties;
    }

    /**
     * Builds the current snapshot from active vehicles and world settings.
     *
     * @return simulation snapshot for API and SSE consumers
     */
    @Override
    public SimulationSnapshot currentSnapshot() {
        List<VehicleState> vehicles = vehicleStateRepository.findAll().stream()
                .filter(vehicle -> vehicle.getStatus() != VehicleStatus.DISCONNECTED)
                .collect(java.util.stream.Collectors.toList());
        SimulationSnapshot snapshot = new SimulationSnapshot();
        snapshot.setGeneratedAt(System.currentTimeMillis());
        snapshot.setVehicles(vehicles);
        snapshot.setWorld(new SimulationWorld(properties.getWorld().getWidth(), properties.getWorld().getHeight()));
        snapshot.setCollisionWarnings((int) vehicles.stream().filter(v -> v.getRiskLevel() == RiskLevel.HIGH).count());
        return snapshot;
    }
}


