package com.example.konecranes.vehicle;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.model.AvoidanceAction;
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
    private final AtomicReference<PositionSnapshot> lastPositionSnapshot = new AtomicReference<>();

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
        lastPositionSnapshot.set(new PositionSnapshot(initial.getX(), initial.getY(), System.currentTimeMillis()));
        this.motionEngine = new VehicleMotionEngine(config, initial.getDirectionDeg());
        this.safetyEngine = new VehicleSafetyEngine(config);
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

        // Check nearby vehicles for immediate threats (defensive copy prevents concurrent modification)
        List<VehicleState> nearby = new ArrayList<>(nearbyVehicles.values());
        VehicleState threat = safetyEngine.findImmediateThreat(state, nearby, nextX, nextY);
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

        // Check if vehicle is stuck and apply emergency escape if needed
        checkAndEscapeIfStuck(state);
    }

    public void aiTick() {
        VehicleState current = selfState.get();
        List<VehicleState> context = new ArrayList<>(nearbyVehicles.values());
        controlPolicy.aiTick(current, context, motionEngine::getTargetDirection, motionEngine::setTargetDirection);
    }

    private void checkAndEscapeIfStuck(VehicleState state) {
        PositionSnapshot snapshot = lastPositionSnapshot.get();
        long now = System.currentTimeMillis();
        double distance = distance(state.getX(), state.getY(), snapshot.x, snapshot.y);
        long timeDelta = now - snapshot.timestamp;

        // If vehicle hasn't moved much and has been stuck for too long, apply escape maneuver
        if (state.getStatus() == VehicleStatus.ACTIVE && 
            timeDelta > config.getStuckTimeMillis() && 
            distance < config.getStuckDistanceThreshold()) {
            // Reverse direction and boost speed to break deadlock
            state.setDirectionDeg(normalizeDirection(state.getDirectionDeg() + 180.0));
            motionEngine.setTargetDirection(state.getDirectionDeg());
            state.setSpeed(config.getInitialSpeed() * config.getStuckEscapeSpeedFactor());
            state.setCurrentAction(AvoidanceAction.EMERGENCY_STOP);
            lastPositionSnapshot.set(new PositionSnapshot(state.getX(), state.getY(), now));
        } else if (timeDelta > config.getStuckTimeMillis()) {
            // Reset snapshot periodically to allow fresh stuck detection
            lastPositionSnapshot.set(new PositionSnapshot(state.getX(), state.getY(), now));
        }
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    private double normalizeDirection(double direction) {
        double normalized = direction % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }

    public VehicleState currentStateCopy() {
        return selfState.get().copy();
    }

    private static class PositionSnapshot {
        final double x;
        final double y;
        final long timestamp;

        PositionSnapshot(double x, double y, long timestamp) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
        }
    }
}



