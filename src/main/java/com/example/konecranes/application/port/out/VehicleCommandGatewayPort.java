package com.example.konecranes.application.port.out;

import com.example.konecranes.messaging.ControlCommand;

import java.io.IOException;

public interface VehicleCommandGatewayPort {
    void sendControlCommand(String vehicleId, ControlCommand command) throws IOException;
}


