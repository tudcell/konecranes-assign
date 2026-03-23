package com.example.konecranes.service;

import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.gateway.VehicleConnectionManager;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.repository.VehicleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class EnvironmentBroadcastService {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentBroadcastService.class);

    private final VehicleRegistry vehicleRegistry;
    private final VehicleConnectionManager connectionManager;

    public EnvironmentBroadcastService(VehicleRegistry vehicleRegistry,
                                       VehicleConnectionManager connectionManager) {
        this.vehicleRegistry = vehicleRegistry;
        this.connectionManager = connectionManager;
    }

    public void broadcastToAll() {
        List<VehicleState> allVehicles = vehicleRegistry.findAll();
        for (VehicleState self : allVehicles) {
            EnvironmentUpdate update = new EnvironmentUpdate();
            update.setTimestamp(System.currentTimeMillis());
            update.setNearbyVehicles(allVehicles.stream()
                    .filter(v -> !v.getId().equals(self.getId()))
                    .map(VehicleState::copy)
                    .collect(java.util.stream.Collectors.toList()));
            try {
                connectionManager.sendEnvironment(self.getId(), update);
            } catch (IOException ex) {
                logger.warn("Failed to send environment update to vehicle {}", self.getId(), ex);
            }
        }
    }
}
