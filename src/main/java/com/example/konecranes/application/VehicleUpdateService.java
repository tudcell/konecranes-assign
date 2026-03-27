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
 * Application service that stores the latest vehicle state updates.
 *
 * Accepts state updates from the application boundary,
 * maps them into the domain state model,
 * and persists the latest version in the repository.
 */
@Service
public class VehicleUpdateService implements UpdateVehicleStateUseCase {

    private final VehicleStateRepository vehicleStateRepository;

    public VehicleUpdateService(VehicleStateRepository vehicleStateRepository) {
        this.vehicleStateRepository = vehicleStateRepository;
    }

    /**
     * Stores one already-built vehicle state and refreshes its timestamp.
     *
     * @param vehicleState vehicle state to persist
     */
    public void updateState(VehicleState vehicleState) {
        vehicleState.setTimestamp(System.currentTimeMillis());
        vehicleStateRepository.upsert(vehicleState);
    }

    /**
     * Converts one update command into a vehicle state
     * and persists the result.
     *
     * Missing nullable fields are replaced with default values:
     * - status -> ACTIVE
     * - currentAction -> KEEP_COURSE
     * - riskLevel -> LOW
     *
     * @param updateVehicleStateCommand incoming vehicle state update
     */
    @Override
    public void updateState(UpdateVehicleStateCommand updateVehicleStateCommand) {
        VehicleState state = new VehicleState();
        state.setId(updateVehicleStateCommand.getVehicleId());
        state.setX(updateVehicleStateCommand.getX());
        state.setY(updateVehicleStateCommand.getY());
        state.setDirectionDeg(updateVehicleStateCommand.getDirectionDeg());
        state.setSpeed(updateVehicleStateCommand.getSpeed());
        state.setRadius(updateVehicleStateCommand.getRadius());
        state.setStatus(updateVehicleStateCommand.getStatus() == null ? VehicleStatus.ACTIVE : updateVehicleStateCommand.getStatus());
        state.setCurrentAction(updateVehicleStateCommand.getCurrentAction() == null ? AvoidanceAction.KEEP_COURSE : updateVehicleStateCommand.getCurrentAction());
        state.setRiskLevel(updateVehicleStateCommand.getRiskLevel() == null ? RiskLevel.LOW : updateVehicleStateCommand.getRiskLevel());
        state.setCurrentRiskScore(updateVehicleStateCommand.getCurrentRiskScore());

        updateState(state);
    }
}