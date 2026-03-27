package com.example.konecranes.vehicle;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;

import java.util.function.DoubleConsumer;

/**
 * Handles last-moment collision prevention.
 *
 * This engine is only responsible for immediate safety reactions.
 * Normal steering and route adjustments belong to the control policy layer.
 */
public class VehicleSafetyEngine {

    private static final double HARD_STOP_EXTRA_MARGIN = 0.5;
    private static final double SOFT_BRAKE_EXTRA_MARGIN = 10.0;

    private final VehicleProcessConfig vehicleProcessConfig;

    public VehicleSafetyEngine(VehicleProcessConfig vehicleProcessConfig) {
        this.vehicleProcessConfig = vehicleProcessConfig;
    }

    /**
     * Finds the nearest immediate collision threat for the next movement step.
     *
     * @param currentState current vehicle state
     * @param nearbyVehicles latest nearby vehicle states
     * @param nextX predicted next x position
     * @param nextY predicted next y position
     * @return nearest threatening vehicle, or null when no immediate threat exists
     */
    public VehicleState findImmediateThreat(VehicleState currentState,
                                            Iterable<VehicleState> nearbyVehicles,
                                            double nextX,
                                            double nextY) {
        VehicleState nearestThreat = null;
        double nearestDistance = Double.MAX_VALUE;

        for (VehicleState nearbyVehicle : nearbyVehicles) {
            if (nearbyVehicle == null || currentState.getId().equals(nearbyVehicle.getId())) {
                continue;
            }

            double currentDistance = distance(
                    currentState.getX(),
                    currentState.getY(),
                    nearbyVehicle.getX(),
                    nearbyVehicle.getY()
            );

            double emergencyDistance =
                    currentState.getRadius()
                            + nearbyVehicle.getRadius()
                            + vehicleProcessConfig.getSafetyEmergencyMargin();

            if (currentDistance < nearestDistance
                    && isEmergencyLikely(currentState, nearbyVehicle, nextX, nextY, emergencyDistance)) {
                nearestDistance = currentDistance;
                nearestThreat = nearbyVehicle;
            }
        }

        return nearestThreat;
    }

    /**
     * Applies an emergency maneuver against a detected threat.
     *
     * Depending on separation, this may:
     * - perform a hard stop
     * - apply soft braking
     * - adjust the target heading away from the threat
     *
     * @param currentState current vehicle state to update
     * @param threatVehicle detected threat vehicle
     * @param targetDirectionSetter callback used to update target steering
     */
    public void applyEmergencyManeuver(VehicleState currentState,
                                       VehicleState threatVehicle,
                                       DoubleConsumer targetDirectionSetter) {
        double dx = currentState.getX() - threatVehicle.getX();
        double dy = currentState.getY() - threatVehicle.getY();

        double separation = distance(
                currentState.getX(),
                currentState.getY(),
                threatVehicle.getX(),
                threatVehicle.getY()
        );

        double hardStopDistance =
                currentState.getRadius()
                        + threatVehicle.getRadius()
                        + HARD_STOP_EXTRA_MARGIN;

        double softBrakeDistance =
                currentState.getRadius()
                        + threatVehicle.getRadius()
                        + SOFT_BRAKE_EXTRA_MARGIN;

        double escapeHeading = Math.toDegrees(Math.atan2(dy, dx));

        if (separation <= hardStopDistance) {
            currentState.setSpeed(Math.max(
                    0.0,
                    currentState.getSpeed() * vehicleProcessConfig.getSafetyHardStopFactor()
            ));
            currentState.setStatus(VehicleStatus.STOPPED);
            currentState.setCurrentAction(AvoidanceAction.EMERGENCY_STOP);
            targetDirectionSetter.accept(escapeHeading);
            return;
        }

        if (separation <= softBrakeDistance) {
            currentState.setSpeed(Math.max(
                    vehicleProcessConfig.getSafetySoftBrakeMinimumSpeed(),
                    currentState.getSpeed() * vehicleProcessConfig.getSafetySoftBrakeFactor()
            ));
            currentState.setStatus(VehicleStatus.ACTIVE);
            currentState.setCurrentAction(AvoidanceAction.SLOW_DOWN);
            targetDirectionSetter.accept(escapeHeading);
            return;
        }

        currentState.setStatus(VehicleStatus.ACTIVE);
        currentState.setCurrentAction(AvoidanceAction.KEEP_COURSE);
    }

    /**
     * Checks whether a collision is likely within the lookahead window.
     *
     * A threat is considered immediate when:
     * - the next predicted step is already too close, or
     * - short lookahead prediction shows the vehicles entering the danger zone
     *
     * @param currentState current vehicle
     * @param nearbyVehicle potential threat vehicle
     * @param selfNextX predicted next x position
     * @param selfNextY predicted next y position
     * @param emergencyDistance minimum safe separation distance
     * @return true when a collision is likely soon
     */
    private boolean isEmergencyLikely(VehicleState currentState,
                                      VehicleState nearbyVehicle,
                                      double selfNextX,
                                      double selfNextY,
                                      double emergencyDistance) {
        double distanceAtNextStep = distance(
                selfNextX,
                selfNextY,
                nearbyVehicle.getX(),
                nearbyVehicle.getY()
        );

        if (distanceAtNextStep <= emergencyDistance) {
            return true;
        }

        double nearbyHeadingRad = Math.toRadians(nearbyVehicle.getDirectionDeg());
        double nearbyFutureX =
                nearbyVehicle.getX()
                        + Math.cos(nearbyHeadingRad)
                        * nearbyVehicle.getSpeed()
                        * vehicleProcessConfig.getSafetyEmergencyLookaheadSeconds();
        double nearbyFutureY =
                nearbyVehicle.getY()
                        + Math.sin(nearbyHeadingRad)
                        * nearbyVehicle.getSpeed()
                        * vehicleProcessConfig.getSafetyEmergencyLookaheadSeconds();

        double selfHeadingRad = Math.toRadians(currentState.getDirectionDeg());
        double selfFutureX =
                currentState.getX()
                        + Math.cos(selfHeadingRad)
                        * currentState.getSpeed()
                        * vehicleProcessConfig.getSafetyEmergencyLookaheadSeconds();
        double selfFutureY =
                currentState.getY()
                        + Math.sin(selfHeadingRad)
                        * currentState.getSpeed()
                        * vehicleProcessConfig.getSafetyEmergencyLookaheadSeconds();

        return distance(selfFutureX, selfFutureY, nearbyFutureX, nearbyFutureY) <= emergencyDistance;
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
}