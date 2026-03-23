package com.example.konecranes.model;

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getDirectionDeg() { return directionDeg; }
    public void setDirectionDeg(double directionDeg) { this.directionDeg = directionDeg; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
    public AvoidanceAction getCurrentAction() { return currentAction; }
    public void setCurrentAction(AvoidanceAction currentAction) { this.currentAction = currentAction; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public double getCurrentRiskScore() { return currentRiskScore; }
    public void setCurrentRiskScore(double currentRiskScore) { this.currentRiskScore = currentRiskScore; }
}
