package com.example.konecranes.messaging;

/**
 * Registration payload sent by a vehicle process when opening a TCP session.
 */
public class RegisterVehicleRequest {
    private String vehicleId;
    private double initialX;
    private double initialY;
    private double initialDirectionDeg;
    private double initialSpeed;
    private double radius;

    /** @return vehicle identifier */
    public String getVehicleId() {
        return vehicleId;
    }

    /** @param vehicleId vehicle identifier */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /** @return initial X coordinate */
    public double getInitialX() {
        return initialX;
    }

    /** @param initialX initial X coordinate */
    public void setInitialX(double initialX) {
        this.initialX = initialX;
    }

    /** @return initial Y coordinate */
    public double getInitialY() {
        return initialY;
    }

    /** @param initialY initial Y coordinate */
    public void setInitialY(double initialY) {
        this.initialY = initialY;
    }

    /** @return initial heading in degrees */
    public double getInitialDirectionDeg() {
        return initialDirectionDeg;
    }

    /** @param initialDirectionDeg initial heading in degrees */
    public void setInitialDirectionDeg(double initialDirectionDeg) {
        this.initialDirectionDeg = initialDirectionDeg;
    }

    /** @return initial speed */
    public double getInitialSpeed() {
        return initialSpeed;
    }

    /** @param initialSpeed initial speed */
    public void setInitialSpeed(double initialSpeed) {
        this.initialSpeed = initialSpeed;
    }

    /** @return collision radius */
    public double getRadius() {
        return radius;
    }

    /** @param radius collision radius */
    public void setRadius(double radius) {
        this.radius = radius;
    }
}
