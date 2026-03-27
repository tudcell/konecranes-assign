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
 * Encapsulates the per-vehicle behavior state and decision logic.
 *
 * This class owns the local vehicle state and coordinates:
 * - movement
 * - control policy updates
 * - immediate safety reactions
 * - stuck detection and recovery
 *
 * Transport and scheduling remain outside this class.
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

    /**
     * Replaces the current nearby-vehicle context with the latest environment update.
     *
     * @param update latest environment snapshot from the coordinator
     */
    public void onEnvironmentUpdate(EnvironmentUpdate update) {
        nearbyVehicles.clear();
        for (VehicleState vehicleState : update.getNearbyVehicles()) {
            nearbyVehicles.put(vehicleState.getId(), vehicleState);
        }
    }

    /**
     * Applies a manual control command to the current vehicle state.
     *
     * @param command inbound control command
     */
    public void onControlCommand(ControlCommand command) {
        controlPolicy.applyControlCommand(command, selfState.get(), motionEngine::setTargetDirection);
    }

    /**
     * Executes one movement tick.
     *
     * This includes:
     * - rotating toward the current target heading
     * - predicting the next position
     * - checking for immediate threats
     * - updating position
     * - applying wall bounce behavior
     * - refreshing timestamp
     * - running stuck detection
     */
    public void movementTick() {
        VehicleState state = selfState.get();
        motionEngine.rotateTowardsTarget(state);

        double dtSeconds = config.getTickMillis() / 1000.0;
        double directionRad = Math.toRadians(state.getDirectionDeg());
        double nextX = state.getX() + Math.cos(directionRad) * state.getSpeed() * dtSeconds;
        double nextY = state.getY() + Math.sin(directionRad) * state.getSpeed() * dtSeconds;

        // Copy nearby states to avoid iteration issues while the map is being updated.
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

        checkAndEscapeIfStuck(state);
    }

    /**
     * Executes one control-policy tick using the latest nearby context.
     *
     * The control policy may:
     * - preserve manual override
     * - choose a new AI action
     * - adjust target direction
     * - adjust speed
     */
    public void aiTick() {
        VehicleState current = selfState.get();
        List<VehicleState> context = new ArrayList<>(nearbyVehicles.values());
        controlPolicy.aiTick(current, context, motionEngine::getTargetDirection, motionEngine::setTargetDirection);
    }

    /**
     * Detects whether the vehicle is stuck and applies a recovery maneuver if needed.
     *
     * A vehicle is treated as stuck when:
     * - it is still active
     * - it has moved less than the configured distance threshold
     * - enough time has passed since the last position snapshot
     *
     * @param state current vehicle state to update
     */
    private void checkAndEscapeIfStuck(VehicleState state) {
        PositionSnapshot snapshot = lastPositionSnapshot.get();
        long now = System.currentTimeMillis();
        double distance = distance(state.getX(), state.getY(), snapshot.x, snapshot.y);
        long timeDelta = now - snapshot.timestamp;

        if (state.getStatus() == VehicleStatus.ACTIVE
                && timeDelta > config.getStuckTimeMillis()
                && distance < config.getStuckDistanceThreshold()) {
            state.setDirectionDeg(normalizeDirection(state.getDirectionDeg() + 180.0));
            motionEngine.setTargetDirection(state.getDirectionDeg());
            state.setSpeed(config.getInitialSpeed() * config.getStuckEscapeSpeedFactor());
            state.setCurrentAction(AvoidanceAction.EMERGENCY_STOP);
            lastPositionSnapshot.set(new PositionSnapshot(state.getX(), state.getY(), now));
        } else if (timeDelta > config.getStuckTimeMillis()) {
            lastPositionSnapshot.set(new PositionSnapshot(state.getX(), state.getY(), now));
        }
    }

    /**
     * Computes Euclidean distance between two points.
     *
     * @param x1 first point x
     * @param y1 first point y
     * @param x2 second point x
     * @param y2 second point y
     * @return distance
     */
    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    /**
     * Normalizes a direction into the range [0, 360).
     *
     * @param direction input angle in degrees
     * @return normalized direction
     */
    private double normalizeDirection(double direction) {
        double normalized = direction % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }

    /**
     * Returns a detached copy of the current vehicle state.
     *
     * Used when state must be exposed outside this engine
     * without sharing the mutable internal instance.
     *
     * @return copied vehicle state
     */
    public VehicleState currentStateCopy() {
        return selfState.get().copy();
    }

    /**
     * Small value object used for stuck detection.
     *
     * Stores the last observed position and timestamp
     * used to measure recent movement progress.
     */
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