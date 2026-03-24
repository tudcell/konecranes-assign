package com.example.konecranes.application.port.in;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleStatus;

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

    public String getVehicleId() {
        return vehicleId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDirectionDeg() {
        return directionDeg;
    }

    public double getSpeed() {
        return speed;
    }

    public double getRadius() {
        return radius;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public AvoidanceAction getCurrentAction() {
        return currentAction;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public double getCurrentRiskScore() {
        return currentRiskScore;
    }
}


