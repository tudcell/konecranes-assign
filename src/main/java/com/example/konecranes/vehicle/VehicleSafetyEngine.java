package com.example.konecranes.vehicle;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;

import java.util.function.DoubleConsumer;

/**
 * Handles only last-moment collision prevention.
 * Normal steering adjustments should be handled by the control/AI layer.
 */
public class VehicleSafetyEngine {
    private final VehicleProcessConfig config;

    public VehicleSafetyEngine(VehicleProcessConfig config) {
        this.config = config;
    }

    /**
     * Detects the nearest immediate collision threat for the next movement step.
     *
     * @param state current self state
     * @param nearbyVehicles latest nearby vehicles
     * @param nextX predicted next X position
     * @param nextY predicted next Y position
     * @return nearest threatening vehicle or null when no immediate threat is found
     */
    public VehicleState findImmediateThreat(VehicleState state,
                                            Iterable<VehicleState> nearbyVehicles,
                                            double nextX,
                                            double nextY) {
        VehicleState nearestThreat = null;
        double nearestDistance = Double.MAX_VALUE;

        for (VehicleState other : nearbyVehicles) {
            if (other == null || state.getId().equals(other.getId())) {
                continue;
            }

            double nowDistance = distance(state.getX(), state.getY(), other.getX(), other.getY());
            double emergencyDistance = state.getRadius() + other.getRadius() + config.getSafetyEmergencyMargin();

            if (nowDistance < nearestDistance && isEmergencyLikely(state, other, nextX, nextY, emergencyDistance)) {
                nearestDistance = nowDistance;
                nearestThreat = other;
            }
        }

        return nearestThreat;
    }

    /**
     * Applies emergency braking/escape action against a detected threat.
     *
     * @param state self vehicle state to mutate
     * @param threat detected threat vehicle
     * @param targetDirectionSetter callback used to update target steering
     */
    public void applyEmergencyManeuver(VehicleState state,
                                       VehicleState threat,
                                       DoubleConsumer targetDirectionSetter) {
        double dx = state.getX() - threat.getX();
        double dy = state.getY() - threat.getY();
        double separation = distance(state.getX(), state.getY(), threat.getX(), threat.getY());
        double hardStopDistance = state.getRadius() + threat.getRadius() + 0.5;
        double softBrakeDistance = state.getRadius() + threat.getRadius() + 10.0;

        double escapeHeading = Math.toDegrees(Math.atan2(dy, dx));

        if (separation <= hardStopDistance) {
            state.setSpeed(Math.max(0.0, state.getSpeed() * config.getSafetyHardStopFactor()));
            state.setStatus(VehicleStatus.STOPPED);
            state.setCurrentAction(AvoidanceAction.EMERGENCY_STOP);
            targetDirectionSetter.accept(escapeHeading);
            return;
        }

        if (separation <= softBrakeDistance) {
            state.setSpeed(Math.max(config.getSafetySoftBrakeMinimumSpeed(), state.getSpeed() * config.getSafetySoftBrakeFactor()));
            state.setStatus(VehicleStatus.ACTIVE);
            state.setCurrentAction(AvoidanceAction.SLOW_DOWN);
            targetDirectionSetter.accept(escapeHeading);
            return;
        }

        state.setStatus(VehicleStatus.ACTIVE);
        state.setCurrentAction(AvoidanceAction.KEEP_COURSE);
    }

    /**
     * Tests whether a collision is imminent within the lookahead window.
     *
     * @param self current vehicle
     * @param other potential threat vehicle
     * @param selfNextX predicted next X position of self
     * @param selfNextY predicted next Y position of self
     * @param emergencyDistance safety boundary distance
     * @return true when collision is likely within lookahead period
     */
    private boolean isEmergencyLikely(VehicleState self,
                                      VehicleState other,
                                      double selfNextX,
                                      double selfNextY,
                                      double emergencyDistance) {
        double distanceAtNextStep = distance(selfNextX, selfNextY, other.getX(), other.getY());
        if (distanceAtNextStep <= emergencyDistance) {
            return true;
        }

        double otherHeadingRad = Math.toRadians(other.getDirectionDeg());
        double otherFutureX = other.getX() + Math.cos(otherHeadingRad) * other.getSpeed() * config.getSafetyEmergencyLookaheadSeconds();
        double otherFutureY = other.getY() + Math.sin(otherHeadingRad) * other.getSpeed() * config.getSafetyEmergencyLookaheadSeconds();

        double selfHeadingRad = Math.toRadians(self.getDirectionDeg());
        double selfFutureX = self.getX() + Math.cos(selfHeadingRad) * self.getSpeed() * config.getSafetyEmergencyLookaheadSeconds();
        double selfFutureY = self.getY() + Math.sin(selfHeadingRad) * self.getSpeed() * config.getSafetyEmergencyLookaheadSeconds();

        return distance(selfFutureX, selfFutureY, otherFutureX, otherFutureY) <= emergencyDistance;
    }

    /**
     * Euclidean distance between two points.
     *
     * @param x1 first point X
     * @param y1 first point Y
     * @param x2 second point X
     * @param y2 second point Y
     * @return distance
     */
    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }
}