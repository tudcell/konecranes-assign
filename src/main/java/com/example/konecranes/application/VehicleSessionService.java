package com.example.konecranes.application;

import com.example.konecranes.application.port.in.DisconnectVehicleSessionCommand;
import com.example.konecranes.application.port.in.DisconnectVehicleSessionUseCase;
import com.example.konecranes.application.port.in.RegisterVehicleSessionCommand;
import com.example.konecranes.application.port.in.RegisterVehicleSessionUseCase;
import com.example.konecranes.application.port.out.VehicleEnvironmentGatewayPort;
import com.example.konecranes.application.port.out.VehicleRegistrationGatewayPort;
import com.example.konecranes.application.port.out.VehicleStateRepository;
import com.example.konecranes.config.SimulationProperties;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.RegisterVehicleAck;
import com.example.konecranes.model.SimulationWorld;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Application service responsible for vehicle session lifecycle.
 *
 * Handles:
 * - initial vehicle registration
 * - initial handshake responses
 * - vehicle disconnect state updates
 */
@Service
public class VehicleSessionService implements RegisterVehicleSessionUseCase, DisconnectVehicleSessionUseCase {

    private final VehicleStateRepository vehicleStateRepository;
    private final VehicleRegistrationGatewayPort vehicleRegistrationGatewayPort;
    private final VehicleEnvironmentGatewayPort vehicleEnvironmentGatewayPort;
    private final SimulationProperties simulationProperties;

    public VehicleSessionService(VehicleStateRepository vehicleStateRepository,
                                 VehicleRegistrationGatewayPort vehicleRegistrationGatewayPort,
                                 VehicleEnvironmentGatewayPort vehicleEnvironmentGatewayPort,
                                 SimulationProperties simulationProperties) {
        this.vehicleStateRepository = vehicleStateRepository;
        this.vehicleRegistrationGatewayPort = vehicleRegistrationGatewayPort;
        this.vehicleEnvironmentGatewayPort = vehicleEnvironmentGatewayPort;
        this.simulationProperties = simulationProperties;
    }

    /**
     * Registers a newly connected vehicle session.
     *
     * Creates and stores the initial vehicle state, then sends:
     * - a registration acknowledgement
     * - the initial environment snapshot for that vehicle
     *
     * @param command registration input
     * @throws IOException when the handshake responses cannot be sent
     */
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

        vehicleStateRepository.upsert(state);

        RegisterVehicleAck ack = new RegisterVehicleAck();
        ack.setVehicleId(command.getVehicleId());
        ack.setWorld(new SimulationWorld(
                simulationProperties.getWorld().getWidth(),
                simulationProperties.getWorld().getHeight()
        ));

        vehicleRegistrationGatewayPort.sendAck(command.getVehicleId(), ack);

        EnvironmentUpdate environment = new EnvironmentUpdate();
        environment.setNearbyVehicles(vehicleStateRepository.findAllExcept(command.getVehicleId()));
        environment.setTimestamp(System.currentTimeMillis());

        vehicleEnvironmentGatewayPort.sendEnvironment(command.getVehicleId(), environment);
    }

    /**
     * Marks a vehicle as disconnected in the repository.
     *
     * If the command or vehicle id is null, nothing happens.
     * If the vehicle does not exist in the repository, nothing happens.
     *
     * @param command disconnect input
     */
    @Override
    public void disconnect(DisconnectVehicleSessionCommand command) {
        if (command == null || command.getVehicleId() == null) {
            return;
        }

        vehicleStateRepository.findById(command.getVehicleId()).ifPresent(state -> {
            state.setStatus(VehicleStatus.DISCONNECTED);
            vehicleStateRepository.upsert(state);
        });
    }
}