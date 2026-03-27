package com.example.konecranes.application;

import com.example.konecranes.application.port.out.VehicleEnvironmentGatewayPort;
import com.example.konecranes.application.port.out.VehicleStateRepository;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Application service that broadcasts environment context to active vehicles.
 *
 * For each active vehicle, this service builds a view of all other
 * active vehicles and sends it through the environment gateway port.
 */
@Service
public class EnvironmentBroadcastService {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentBroadcastService.class);

    private final VehicleStateRepository vehicleStateRepository;
    private final VehicleEnvironmentGatewayPort vehicleEnvironmentGatewayPort;

    public EnvironmentBroadcastService(VehicleStateRepository vehicleStateRepository,
                                       VehicleEnvironmentGatewayPort vehicleEnvironmentGatewayPort) {
        this.vehicleStateRepository = vehicleStateRepository;
        this.vehicleEnvironmentGatewayPort = vehicleEnvironmentGatewayPort;
    }

    /**
     * Broadcasts the latest nearby-vehicle context to every active vehicle.
     *
     * Each active vehicle receives:
     * - the current timestamp
     * - a list of all other active vehicles
     *
     * Vehicles marked as disconnected or stopped are excluded
     * from the outer broadcast loop unless they are ACTIVE.
     */
    public void broadcastToAll() {
        List<VehicleState> activeVehicles = vehicleStateRepository.findAll().stream()
                .filter(vehicle -> vehicle.getStatus() == VehicleStatus.ACTIVE)
                .collect(java.util.stream.Collectors.toList());

        for (VehicleState self : activeVehicles) {
            EnvironmentUpdate update = new EnvironmentUpdate();
            update.setTimestamp(System.currentTimeMillis());
            update.setNearbyVehicles(activeVehicles.stream()
                    .filter(vehicle -> !vehicle.getId().equals(self.getId()))
                    .map(VehicleState::copy)
                    .collect(java.util.stream.Collectors.toList()));

            try {
                vehicleEnvironmentGatewayPort.sendEnvironment(self.getId(), update);
            } catch (IOException ex) {
                logger.warn("Failed to send environment update to vehicle {}", self.getId(), ex);
            }
        }
    }
}