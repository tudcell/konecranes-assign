package com.example.konecranes.application;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import com.example.konecranes.application.port.in.UpdateVehicleStateCommand;
import com.example.konecranes.application.port.in.UpdateVehicleStateUseCase;
import com.example.konecranes.application.port.out.VehicleStateStore;
import org.springframework.stereotype.Service;

@Service
public class VehicleUpdateService implements UpdateVehicleStateUseCase {

    private final VehicleStateStore vehicleStateStore;

    public VehicleUpdateService(VehicleStateStore vehicleStateStore) {
        this.vehicleStateStore = vehicleStateStore;
    }

    public void updateState(VehicleState state) {
        state.setTimestamp(System.currentTimeMillis());
        vehicleStateStore.upsert(state);
    }

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

