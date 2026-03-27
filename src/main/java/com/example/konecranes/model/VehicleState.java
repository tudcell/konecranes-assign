package com.example.konecranes.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Mutable snapshot of one vehicle state.
 *
 * Used to exchange the latest vehicle data between
 * the coordinator and vehicle processes.
 */
@Getter
@Setter
public class VehicleState {

    private String id;
    private double x;
    private double y;
    private double directionDeg;
    private double speed;
    private double radius;
    private long timestamp;
    private VehicleStatus status = VehicleStatus.ACTIVE;
    private AvoidanceAction currentAction = AvoidanceAction.KEEP_COURSE;
    private RiskLevel riskLevel = RiskLevel.LOW;
    private double currentRiskScore;

    /**
     * Creates a detached copy of this vehicle state.
     *
     * Used to avoid sharing the same mutable instance
     * across threads or layers.
     *
     * @return copied vehicle state
     */
    public VehicleState copy() {
        VehicleState copy = new VehicleState();
        copy.id = this.id;
        copy.x = this.x;
        copy.y = this.y;
        copy.directionDeg = this.directionDeg;
        copy.speed = this.speed;
        copy.radius = this.radius;
        copy.timestamp = this.timestamp;
        copy.status = this.status;
        copy.currentAction = this.currentAction;
        copy.riskLevel = this.riskLevel;
        copy.currentRiskScore = this.currentRiskScore;
        return copy;
    }
}