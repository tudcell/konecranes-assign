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

    private static final double EMERGENCY_MARGIN = 3.0;
    private static final double EMERGENCY_LOOKAHEAD_SECONDS = 0.08;
    private static final double BRAKE_LOOKAHEAD_SECONDS = 0.15;

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
            double emergencyDistance = state.getRadius() + other.getRadius() + EMERGENCY_MARGIN;

            if (nowDistance < nearestDistance && isEmergencyLikely(state, other, nextX, nextY, emergencyDistance)) {
                nearestDistance = nowDistance;
                nearestThreat = other;
            }
        }

        return nearestThreat;
    }

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
            state.setSpeed(Math.max(0.0, state.getSpeed() * 0.25));
            state.setStatus(VehicleStatus.STOPPED);
            state.setCurrentAction(AvoidanceAction.EMERGENCY_STOP);
            targetDirectionSetter.accept(escapeHeading);
            return;
        }

        if (separation <= softBrakeDistance) {
            state.setSpeed(Math.max(20.0, state.getSpeed() * 0.80));
            state.setStatus(VehicleStatus.ACTIVE);
            state.setCurrentAction(AvoidanceAction.SLOW_DOWN);
            targetDirectionSetter.accept(escapeHeading);
            return;
        }

        state.setStatus(VehicleStatus.ACTIVE);
        state.setCurrentAction(AvoidanceAction.KEEP_COURSE);
    }

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
        double otherFutureX = other.getX() + Math.cos(otherHeadingRad) * other.getSpeed() * EMERGENCY_LOOKAHEAD_SECONDS;
        double otherFutureY = other.getY() + Math.sin(otherHeadingRad) * other.getSpeed() * EMERGENCY_LOOKAHEAD_SECONDS;

        double selfHeadingRad = Math.toRadians(self.getDirectionDeg());
        double selfFutureX = self.getX() + Math.cos(selfHeadingRad) * self.getSpeed() * EMERGENCY_LOOKAHEAD_SECONDS;
        double selfFutureY = self.getY() + Math.sin(selfHeadingRad) * self.getSpeed() * EMERGENCY_LOOKAHEAD_SECONDS;

        return distance(selfFutureX, selfFutureY, otherFutureX, otherFutureY) <= emergencyDistance;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }
}