package com.example.konecranes.application.port.in;

import java.io.IOException;

/**
 * Sends manual override commands to vehicle processes.
 */
public interface VehicleControlUseCase {

    /**
     * Overrides the target direction for one vehicle.
     *
     * @param vehicleId vehicle id to control
     * @param directionDeg desired heading in degrees
     * @throws IOException when command dispatch fails
     */
    void overrideDirection(String vehicleId, double directionDeg) throws IOException;

    /**
     * Overrides the speed for one vehicle.
     *
     * @param vehicleId vehicle id to control
     * @param speed desired speed value
     * @throws IOException when command dispatch fails
     */
    void overrideSpeed(String vehicleId, double speed) throws IOException;
}
