package com.example.konecranes.application.port.in;

public class RegisterVehicleSessionCommand {
    private final String vehicleId;
    private final double initialX;
    private final double initialY;
    private final double initialDirectionDeg;
    private final double initialSpeed;
    private final double radius;

    public RegisterVehicleSessionCommand(String vehicleId,
                                         double initialX,
                                         double initialY,
                                         double initialDirectionDeg,
                                         double initialSpeed,
                                         double radius) {
        this.vehicleId = vehicleId;
        this.initialX = initialX;
        this.initialY = initialY;
        this.initialDirectionDeg = initialDirectionDeg;
        this.initialSpeed = initialSpeed;
        this.radius = radius;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public double getInitialX() {
        return initialX;
    }

    public double getInitialY() {
        return initialY;
    }

    public double getInitialDirectionDeg() {
        return initialDirectionDeg;
    }

    public double getInitialSpeed() {
        return initialSpeed;
    }

    public double getRadius() {
        return radius;
    }
}


