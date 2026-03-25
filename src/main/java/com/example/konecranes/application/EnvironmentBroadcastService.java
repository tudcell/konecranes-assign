package com.example.konecranes.application;

import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import com.example.konecranes.application.port.out.VehicleEnvironmentGatewayPort;
import com.example.konecranes.application.port.out.VehicleStateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class EnvironmentBroadcastService {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentBroadcastService.class);

    private final VehicleStateStore vehicleStateStore;
    private final VehicleEnvironmentGatewayPort environmentGatewayPort;

    public EnvironmentBroadcastService(VehicleStateStore vehicleStateStore,
                                       VehicleEnvironmentGatewayPort environmentGatewayPort) {
        this.vehicleStateStore = vehicleStateStore;
        this.environmentGatewayPort = environmentGatewayPort;
    }

    public void broadcastToAll() {
        List<VehicleState> activeVehicles = vehicleStateStore.findAll().stream()
                .filter(vehicle -> vehicle.getStatus() == VehicleStatus.ACTIVE)
                .collect(java.util.stream.Collectors.toList());

        for (VehicleState self : activeVehicles) {
            EnvironmentUpdate update = new EnvironmentUpdate();
            update.setTimestamp(System.currentTimeMillis());
            update.setNearbyVehicles(activeVehicles.stream()
                    .filter(v -> !v.getId().equals(self.getId()))
                    .map(VehicleState::copy)
                    .collect(java.util.stream.Collectors.toList()));
            try {
                environmentGatewayPort.sendEnvironment(self.getId(), update);
            } catch (IOException ex) {
                logger.warn("Failed to send environment update to vehicle {}", self.getId(), ex);
            }
        }
    }
}

