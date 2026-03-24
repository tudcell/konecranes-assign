package com.example.konecranes.application.port.out;

import com.example.konecranes.messaging.EnvironmentUpdate;

import java.io.IOException;

public interface VehicleEnvironmentGatewayPort {
    void sendEnvironment(String vehicleId, EnvironmentUpdate update) throws IOException;
}


