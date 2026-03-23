package com.example.konecranes.service;

import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.RegisterVehicleAck;
import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.model.SimulationWorld;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;

@Service
public class VehicleSessionService {

    private final VehicleRegistry vehicleRegistry;
    private final VehicleConnectionManager connectionManager;
    private final SimulationProperties properties;

    public VehicleSessionService(VehicleRegistry vehicleRegistry,
                                 VehicleConnectionManager connectionManager,
                                 SimulationProperties properties) {
        this.vehicleRegistry = vehicleRegistry;
        this.connectionManager = connectionManager;
        this.properties = properties;
    }

    public void register(RegisterVehicleRequest request, BufferedWriter writer) throws IOException {
        VehicleState state = new VehicleState();
        state.setId(request.getVehicleId());
        state.setX(request.getInitialX());
        state.setY(request.getInitialY());
        state.setDirectionDeg(request.getInitialDirectionDeg());
        state.setSpeed(request.getInitialSpeed());
        state.setRadius(request.getRadius());
        state.setTimestamp(System.currentTimeMillis());
        state.setStatus(VehicleStatus.ACTIVE);

        vehicleRegistry.upsert(state);
        connectionManager.attach(request.getVehicleId(), writer);

        RegisterVehicleAck ack = new RegisterVehicleAck();
        ack.setVehicleId(request.getVehicleId());
        ack.setWorld(new SimulationWorld(properties.getWorld().getWidth(), properties.getWorld().getHeight()));
        connectionManager.sendAck(request.getVehicleId(), ack);

        EnvironmentUpdate environment = new EnvironmentUpdate();
        environment.setNearbyVehicles(vehicleRegistry.findAllExcept(request.getVehicleId()));
        environment.setTimestamp(System.currentTimeMillis());
        connectionManager.sendEnvironment(request.getVehicleId(), environment);
    }

    public void disconnect(String vehicleId) {
        vehicleRegistry.findById(vehicleId).ifPresent(state -> {
            state.setStatus(VehicleStatus.DISCONNECTED);
            vehicleRegistry.upsert(state);
        });
        connectionManager.detach(vehicleId);
    }
}
