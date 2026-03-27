package com.example.konecranes.application.port.in;

import lombok.Getter;

/**
 * Input command used to register a newly connected vehicle.
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
     * @param vehicleId vehicle identifier
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
