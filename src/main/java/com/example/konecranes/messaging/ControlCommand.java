package com.example.konecranes.messaging;

/**
 * Command payload used to apply manual control on a vehicle process.
 */
public class ControlCommand {
    private String vehicleId;
    private Double overrideDirectionDeg;
    private Double overrideSpeed;
    private boolean manualOverride;

    /** @return target vehicle id */
    public String getVehicleId() {
        return vehicleId;
    }

    /** @param vehicleId target vehicle id */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /** @return optional heading override in degrees */
    public Double getOverrideDirectionDeg() {
        return overrideDirectionDeg;
    }

    /** @param overrideDirectionDeg heading override in degrees */
    public void setOverrideDirectionDeg(Double overrideDirectionDeg) {
        this.overrideDirectionDeg = overrideDirectionDeg;
    }

    /** @return optional speed override */
    public Double getOverrideSpeed() {
        return overrideSpeed;
    }

    /** @param overrideSpeed speed override */
    public void setOverrideSpeed(Double overrideSpeed) {
        this.overrideSpeed = overrideSpeed;
    }

    /** @return true when command should activate manual override mode */
    public boolean isManualOverride() {
        return manualOverride;
    }

    /** @param manualOverride true to activate manual override mode */
    public void setManualOverride(boolean manualOverride) {
        this.manualOverride = manualOverride;
    }
}
