package com.example.konecranes.application;

import com.example.konecranes.application.port.in.SimulationQueryUseCase;
import com.example.konecranes.application.port.out.VehicleStateRepository;
import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.SimulationSnapshot;
import com.example.konecranes.model.SimulationWorld;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that builds the current simulation snapshot.
 *
 * Produces the read model used by the REST API and SSE stream.
 */
@Service
public class SimulationSnapshotService implements SimulationQueryUseCase {

    private final VehicleStateRepository vehicleStateRepository;
    private final SimulationProperties simulationProperties;

    public SimulationSnapshotService(VehicleStateRepository vehicleStateRepository,
                                     SimulationProperties simulationProperties) {
        this.vehicleStateRepository = vehicleStateRepository;
        this.simulationProperties = simulationProperties;
    }

    /**
     * Builds the latest simulation snapshot.
     *
     * The snapshot contains:
     * - all non-disconnected vehicles
     * - current world dimensions
     * - a generated timestamp
     * - the number of vehicles currently marked as high risk
     *
     * @return current simulation snapshot
     */
    @Override
    public SimulationSnapshot currentSnapshot() {
        List<VehicleState> vehicles = vehicleStateRepository.findAll().stream()
                .filter(vehicle -> vehicle.getStatus() != VehicleStatus.DISCONNECTED)
                .collect(java.util.stream.Collectors.toList());

        SimulationSnapshot snapshot = new SimulationSnapshot();
        snapshot.setGeneratedAt(System.currentTimeMillis());
        snapshot.setVehicles(vehicles);
        snapshot.setWorld(new SimulationWorld(
                simulationProperties.getWorld().getWidth(),
                simulationProperties.getWorld().getHeight()
        ));
        snapshot.setCollisionWarnings(
                (int) vehicles.stream()
                        .filter(vehicle -> vehicle.getRiskLevel() == RiskLevel.HIGH)
                        .count()
        );

        return snapshot;
    }
}