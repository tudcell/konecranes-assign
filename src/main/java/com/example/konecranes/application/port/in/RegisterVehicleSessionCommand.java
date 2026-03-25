package com.example.konecranes.application.port.in;

/**
 * Input command used to register a newly connected vehicle.
 */
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

    /** @return vehicle identifier */
    public String getVehicleId() {
        return vehicleId;
    }

    /** @return initial X coordinate */
    public double getInitialX() {
        return initialX;
    }

    /** @return initial Y coordinate */
    public double getInitialY() {
        return initialY;
    }

    /** @return initial heading in degrees */
    public double getInitialDirectionDeg() {
        return initialDirectionDeg;
    }

    /** @return initial speed */
    public double getInitialSpeed() {
        return initialSpeed;
    }

    /** @return collision radius */
    public double getRadius() {
        return radius;
    }
}
