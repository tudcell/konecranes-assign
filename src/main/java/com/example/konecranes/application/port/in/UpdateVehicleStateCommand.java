package com.example.konecranes.application.port.in;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleStatus;

/**
 * Input command containing one full vehicle state update.
 */
public class UpdateVehicleStateCommand {
    private final String vehicleId;
    private final double x;
    private final double y;
    private final double directionDeg;
    private final double speed;
    private final double radius;
    private final VehicleStatus status;
    private final AvoidanceAction currentAction;
    private final RiskLevel riskLevel;
    private final double currentRiskScore;

    /**
     * @param vehicleId vehicle identifier
     * @param x world X coordinate
     * @param y world Y coordinate
     * @param directionDeg current heading in degrees
     * @param speed current speed
     * @param radius collision radius
     * @param status current lifecycle status
     * @param currentAction current AI/manual action
     * @param riskLevel current risk tier
     * @param currentRiskScore continuous risk score
     */
    public UpdateVehicleStateCommand(String vehicleId,
                                     double x,
                                     double y,
                                     double directionDeg,
                                     double speed,
                                     double radius,
                                     VehicleStatus status,
                                     AvoidanceAction currentAction,
                                     RiskLevel riskLevel,
                                     double currentRiskScore) {
        this.vehicleId = vehicleId;
        this.x = x;
        this.y = y;
        this.directionDeg = directionDeg;
        this.speed = speed;
        this.radius = radius;
        this.status = status;
        this.currentAction = currentAction;
        this.riskLevel = riskLevel;
        this.currentRiskScore = currentRiskScore;
    }

    /** @return vehicle identifier */
    public String getVehicleId() {
        return vehicleId;
    }

    /** @return X coordinate */
    public double getX() {
        return x;
    }

    /** @return Y coordinate */
    public double getY() {
        return y;
    }

    /** @return heading in degrees */
    public double getDirectionDeg() {
        return directionDeg;
    }

    /** @return speed */
    public double getSpeed() {
        return speed;
    }

    /** @return collision radius */
    public double getRadius() {
        return radius;
    }

    /** @return lifecycle status */
    public VehicleStatus getStatus() {
        return status;
    }

    /** @return current action */
    public AvoidanceAction getCurrentAction() {
        return currentAction;
    }

    /** @return risk level */
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    /** @return continuous risk score */
    public double getCurrentRiskScore() {
        return currentRiskScore;
    }
}
