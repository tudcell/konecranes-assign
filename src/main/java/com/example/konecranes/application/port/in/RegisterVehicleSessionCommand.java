package com.example.konecranes.application.port.in;

import lombok.Getter;

/**
 * Command object representing one newly connected vehicle session.
 *
 * Carries the vehicle data needed by the application layer
 * to register the session.
 */
@Getter
public class RegisterVehicleSessionCommand {

    private final String vehicleId;

    private final double initialX;

    private final double initialY;

    private final double initialDirectionDeg;

    private final double initialSpeed;

    private final double radius;

    /**
     * Creates a register command for one vehicle session.
     *
     * @param vehicleId unique vehicle identifier
     * @param initialX initial world X coordinate
     * @param initialY initial world Y coordinate
     * @param initialDirectionDeg initial heading in degrees
     * @param initialSpeed initial speed value
     * @param radius collision radius
     */
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
}