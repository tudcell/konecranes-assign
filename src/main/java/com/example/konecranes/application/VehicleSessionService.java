package com.example.konecranes.application;

import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.RegisterVehicleAck;
import com.example.konecranes.model.SimulationWorld;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import com.example.konecranes.application.port.in.DisconnectVehicleSessionCommand;
import com.example.konecranes.application.port.in.DisconnectVehicleSessionUseCase;
import com.example.konecranes.application.port.in.RegisterVehicleSessionCommand;
import com.example.konecranes.application.port.in.RegisterVehicleSessionUseCase;
import com.example.konecranes.application.port.out.VehicleEnvironmentGatewayPort;
import com.example.konecranes.application.port.out.VehicleRegistrationGatewayPort;
import com.example.konecranes.application.port.out.VehicleStateStore;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VehicleSessionService implements RegisterVehicleSessionUseCase, DisconnectVehicleSessionUseCase {

    private final VehicleStateStore vehicleStateStore;
    private final VehicleRegistrationGatewayPort registrationGatewayPort;
    private final VehicleEnvironmentGatewayPort environmentGatewayPort;
    private final SimulationProperties properties;

    public VehicleSessionService(VehicleStateStore vehicleStateStore,
                                 VehicleRegistrationGatewayPort registrationGatewayPort,
                                 VehicleEnvironmentGatewayPort environmentGatewayPort,
                                 SimulationProperties properties) {
        this.vehicleStateStore = vehicleStateStore;
        this.registrationGatewayPort = registrationGatewayPort;
        this.environmentGatewayPort = environmentGatewayPort;
        this.properties = properties;
    }

    @Override
    public void register(RegisterVehicleSessionCommand command) throws IOException {
        VehicleState state = new VehicleState();
        state.setId(command.getVehicleId());
        state.setX(command.getInitialX());
        state.setY(command.getInitialY());
        state.setDirectionDeg(command.getInitialDirectionDeg());
        state.setSpeed(command.getInitialSpeed());
        state.setRadius(command.getRadius());
        state.setTimestamp(System.currentTimeMillis());
        state.setStatus(VehicleStatus.ACTIVE);

        vehicleStateStore.upsert(state);

        RegisterVehicleAck ack = new RegisterVehicleAck();
        ack.setVehicleId(command.getVehicleId());
        ack.setWorld(new SimulationWorld(properties.getWorld().getWidth(), properties.getWorld().getHeight()));
        registrationGatewayPort.sendAck(command.getVehicleId(), ack);

        EnvironmentUpdate environment = new EnvironmentUpdate();
        environment.setNearbyVehicles(vehicleStateStore.findAllExcept(command.getVehicleId()));
        environment.setTimestamp(System.currentTimeMillis());
        environmentGatewayPort.sendEnvironment(command.getVehicleId(), environment);
    }

    @Override
    public void disconnect(DisconnectVehicleSessionCommand command) {
        if (command == null || command.getVehicleId() == null) {
            return;
        }
        vehicleStateStore.findById(command.getVehicleId()).ifPresent(state -> {
            state.setStatus(VehicleStatus.DISCONNECTED);
            vehicleStateStore.upsert(state);
        });
    }
}


