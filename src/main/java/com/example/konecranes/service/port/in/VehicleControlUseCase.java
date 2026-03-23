package com.example.konecranes.service.port.in;

import java.io.IOException;

public interface VehicleControlUseCase {
    void overrideDirection(String vehicleId, double directionDeg) throws IOException;

    void overrideSpeed(String vehicleId, double speed) throws IOException;
}

