package com.example.konecranes.messaging;

public class RegisterVehicleRequest {
    private String vehicleId;
    private double initialX;
    private double initialY;
    private double initialDirectionDeg;
    private double initialSpeed;
    private double radius;

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public double getInitialX() { return initialX; }
    public void setInitialX(double initialX) { this.initialX = initialX; }
    public double getInitialY() { return initialY; }
    public void setInitialY(double initialY) { this.initialY = initialY; }
    public double getInitialDirectionDeg() { return initialDirectionDeg; }
    public void setInitialDirectionDeg(double initialDirectionDeg) { this.initialDirectionDeg = initialDirectionDeg; }
    public double getInitialSpeed() { return initialSpeed; }
    public void setInitialSpeed(double initialSpeed) { this.initialSpeed = initialSpeed; }
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
}
