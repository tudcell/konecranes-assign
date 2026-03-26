package com.example.konecranes.application;

import com.example.konecranes.application.port.in.UpdateVehicleStateCommand;
import com.example.konecranes.application.port.in.UpdateVehicleStateUseCase;
import com.example.konecranes.application.port.out.VehicleStateRepository;
import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.springframework.stereotype.Service;

/**
 * Application service that persists incoming vehicle state updates.
 */
@Service
public class VehicleUpdateService implements UpdateVehicleStateUseCase {

    private final VehicleStateRepository vehicleStateRepository;

    public VehicleUpdateService(VehicleStateRepository vehicleStateRepository) {
        this.vehicleStateRepository = vehicleStateRepository;
    }

    /**
     * Persists one already-built domain state and refreshes timestamp.
     *
     * @param state vehicle state to store
     */
    public void updateState(VehicleState state) {
        state.setTimestamp(System.currentTimeMillis());
        vehicleStateRepository.upsert(state);
    }

    /**
     * Maps a transport command into domain model and persists it.
     *
     * @param command incoming state command
     */
    @Override
    public void updateState(UpdateVehicleStateCommand command) {
        VehicleState state = new VehicleState();
        state.setId(command.getVehicleId());
        state.setX(command.getX());
        state.setY(command.getY());
        state.setDirectionDeg(command.getDirectionDeg());
        state.setSpeed(command.getSpeed());
        state.setRadius(command.getRadius());
        state.setStatus(command.getStatus() == null ? VehicleStatus.ACTIVE : command.getStatus());
        state.setCurrentAction(command.getCurrentAction() == null ? AvoidanceAction.KEEP_COURSE : command.getCurrentAction());
        state.setRiskLevel(command.getRiskLevel() == null ? RiskLevel.LOW : command.getRiskLevel());
        state.setCurrentRiskScore(command.getCurrentRiskScore());
        updateState(state);
    }
}
