package com.example.konecranes.vehicle;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;

import java.util.function.DoubleConsumer;

/**
 * Handles immediate collision checks and emergency maneuvers.
 */
public class VehicleSafetyEngine {

    private static final double COLLISION_GUARD_MARGIN = 8.0;
    private static final double COLLISION_LOOKAHEAD_SECONDS = 0.25;

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
            double collisionDistance = state.getRadius() + other.getRadius() + COLLISION_GUARD_MARGIN;
            if (nowDistance < nearestDistance && isCollisionLikely(state, other, nextX, nextY, collisionDistance)) {
                nearestDistance = nowDistance;
                nearestThreat = other;
            }
        }

        return nearestThreat;
    }

    public void applyEmergencyManeuver(VehicleState state, VehicleState threat, DoubleConsumer targetDirectionSetter) {
        double dx = state.getX() - threat.getX();
        double dy = state.getY() - threat.getY();
        double separation = distance(state.getX(), state.getY(), threat.getX(), threat.getY());
        double minimumSeparation = state.getRadius() + threat.getRadius() + 2.0;

        if (separation < minimumSeparation) {
            state.setSpeed(0.0);
            state.setStatus(VehicleStatus.STOPPED);
            state.setCurrentAction(AvoidanceAction.EMERGENCY_STOP);
            targetDirectionSetter.accept(Math.toDegrees(Math.atan2(dy, dx)));
            return;
        }

        state.setStatus(VehicleStatus.ACTIVE);
        state.setCurrentAction(AvoidanceAction.SLOW_DOWN);
        state.setSpeed(Math.max(20.0, state.getSpeed() * 0.7));
        targetDirectionSetter.accept(Math.toDegrees(Math.atan2(dy, dx)));
    }

    private boolean isCollisionLikely(VehicleState self,
                                      VehicleState other,
                                      double selfNextX,
                                      double selfNextY,
                                      double collisionDistance) {
        double distanceAtNextStep = distance(selfNextX, selfNextY, other.getX(), other.getY());
        if (distanceAtNextStep <= collisionDistance) {
            return true;
        }

        double otherHeadingRad = Math.toRadians(other.getDirectionDeg());
        double otherFutureX = other.getX() + Math.cos(otherHeadingRad) * other.getSpeed() * COLLISION_LOOKAHEAD_SECONDS;
        double otherFutureY = other.getY() + Math.sin(otherHeadingRad) * other.getSpeed() * COLLISION_LOOKAHEAD_SECONDS;

        double selfHeadingRad = Math.toRadians(self.getDirectionDeg());
        double selfFutureX = self.getX() + Math.cos(selfHeadingRad) * self.getSpeed() * COLLISION_LOOKAHEAD_SECONDS;
        double selfFutureY = self.getY() + Math.sin(selfHeadingRad) * self.getSpeed() * COLLISION_LOOKAHEAD_SECONDS;

        return distance(selfFutureX, selfFutureY, otherFutureX, otherFutureY) <= collisionDistance;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }
}

