package com.example.konecranes.model;

/**
 * Mutable vehicle state snapshot exchanged between coordinator and vehicle process.
 */
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
     * Creates a deep-enough copy for safe cross-thread/cross-layer use.
     *
     * @return detached state copy
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

    /** @return vehicle id */
    public String getId() { return id; }
    /** @param id vehicle id */
    public void setId(String id) { this.id = id; }
    /** @return world X */
    public double getX() { return x; }
    /** @param x world X */
    public void setX(double x) { this.x = x; }
    /** @return world Y */
    public double getY() { return y; }
    /** @param y world Y */
    public void setY(double y) { this.y = y; }
    /** @return heading in degrees */
    public double getDirectionDeg() { return directionDeg; }
    /** @param directionDeg heading in degrees */
    public void setDirectionDeg(double directionDeg) { this.directionDeg = directionDeg; }
    /** @return speed */
    public double getSpeed() { return speed; }
    /** @param speed speed */
    public void setSpeed(double speed) { this.speed = speed; }
    /** @return collision radius */
    public double getRadius() { return radius; }
    /** @param radius collision radius */
    public void setRadius(double radius) { this.radius = radius; }
    /** @return sample timestamp in epoch milliseconds */
    public long getTimestamp() { return timestamp; }
    /** @param timestamp sample timestamp in epoch milliseconds */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    /** @return vehicle lifecycle status */
    public VehicleStatus getStatus() { return status; }
    /** @param status vehicle lifecycle status */
    public void setStatus(VehicleStatus status) { this.status = status; }
    /** @return current control action */
    public AvoidanceAction getCurrentAction() { return currentAction; }
    /** @param currentAction current control action */
    public void setCurrentAction(AvoidanceAction currentAction) { this.currentAction = currentAction; }
    /** @return current risk level */
    public RiskLevel getRiskLevel() { return riskLevel; }
    /** @param riskLevel current risk level */
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    /** @return numeric risk score */
    public double getCurrentRiskScore() { return currentRiskScore; }
    /** @param currentRiskScore numeric risk score */
    public void setCurrentRiskScore(double currentRiskScore) { this.currentRiskScore = currentRiskScore; }
}
