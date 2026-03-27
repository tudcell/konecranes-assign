package com.example.konecranes.application.port.in;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleStatus;
import lombok.Getter;

/**
 * Command carrying one complete vehicle state update.
 *
 * Used by the application layer to receive the latest state
 * reported by a vehicle session.
 */
@Getter
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
     * Creates one vehicle state update command.
     *
     * @param vehicleId vehicle identifier
     * @param x current world x coordinate
     * @param y current world y coordinate
     * @param directionDeg current heading in degrees
     * @param speed current speed
     * @param radius vehicle collision radius
     * @param status current vehicle status
     * @param currentAction current avoidance or manual action
     * @param riskLevel current risk level
     * @param currentRiskScore current numeric risk score
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
}