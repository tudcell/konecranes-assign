package com.example.konecranes.vehicle;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Encapsulates per-vehicle behavior state and decision logic.
 * Transport and scheduling stay in VehicleProcessRuntime.
 */
public class VehicleBehaviorEngine {

    private final VehicleProcessConfig config;
    private final ConcurrentMap<String, VehicleState> nearbyVehicles = new ConcurrentHashMap<>();
    private final AtomicReference<VehicleState> selfState = new AtomicReference<>();
    private final VehicleMotionEngine motionEngine;
    private final VehicleSafetyEngine safetyEngine;
    private final VehicleControlPolicy controlPolicy;

    public VehicleBehaviorEngine(VehicleProcessConfig config) {
        this.config = config;

        VehicleState initial = new VehicleState();
        initial.setId(config.getVehicleId());
        initial.setX(config.getInitialX());
        initial.setY(config.getInitialY());
        initial.setDirectionDeg(config.getInitialDirectionDeg());
        initial.setSpeed(config.getInitialSpeed());
        initial.setRadius(config.getRadius());
        initial.setStatus(VehicleStatus.ACTIVE);
        initial.setTimestamp(System.currentTimeMillis());

        selfState.set(initial);
        this.motionEngine = new VehicleMotionEngine(config, initial.getDirectionDeg());
        this.safetyEngine = new VehicleSafetyEngine();
        this.controlPolicy = new VehicleControlPolicy(config);
    }

    public void onEnvironmentUpdate(EnvironmentUpdate update) {
        nearbyVehicles.clear();
        for (VehicleState vehicleState : update.getNearbyVehicles()) {
            nearbyVehicles.put(vehicleState.getId(), vehicleState);
        }
    }

    public void onControlCommand(ControlCommand command) {
        controlPolicy.applyControlCommand(command, selfState.get(), motionEngine::setTargetDirection);
    }

    public void movementTick() {
        VehicleState state = selfState.get();
        motionEngine.rotateTowardsTarget(state);

        double dtSeconds = config.getTickMillis() / 1000.0;
        double directionRad = Math.toRadians(state.getDirectionDeg());
        double nextX = state.getX() + Math.cos(directionRad) * state.getSpeed() * dtSeconds;
        double nextY = state.getY() + Math.sin(directionRad) * state.getSpeed() * dtSeconds;

        VehicleState threat = safetyEngine.findImmediateThreat(state, nearbyVehicles.values(), nextX, nextY);
        if (threat != null) {
            safetyEngine.applyEmergencyManeuver(state, threat, motionEngine::setTargetDirection);
            motionEngine.bounceIfNeeded(state);
            state.setTimestamp(System.currentTimeMillis());
            return;
        }

        state.setX(nextX);
        state.setY(nextY);

        motionEngine.bounceIfNeeded(state);
        state.setTimestamp(System.currentTimeMillis());
    }

    public void aiTick() {
        VehicleState current = selfState.get();
        List<VehicleState> context = new ArrayList<>(nearbyVehicles.values());
        controlPolicy.aiTick(current, context, motionEngine::getTargetDirection, motionEngine::setTargetDirection);
    }

    public VehicleState currentStateCopy() {
        return selfState.get().copy();
    }
}



