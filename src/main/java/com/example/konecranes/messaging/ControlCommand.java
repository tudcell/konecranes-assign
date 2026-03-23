package com.example.konecranes.messaging;

public class ControlCommand {
    private String vehicleId;
    private Double overrideDirectionDeg;
    private Double overrideSpeed;
    private boolean manualOverride;

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public Double getOverrideDirectionDeg() { return overrideDirectionDeg; }
    public void setOverrideDirectionDeg(Double overrideDirectionDeg) { this.overrideDirectionDeg = overrideDirectionDeg; }
    public Double getOverrideSpeed() { return overrideSpeed; }
    public void setOverrideSpeed(Double overrideSpeed) { this.overrideSpeed = overrideSpeed; }
    public boolean isManualOverride() { return manualOverride; }
    public void setManualOverride(boolean manualOverride) { this.manualOverride = manualOverride; }
}
