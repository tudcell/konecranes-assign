package com.example.konecranes.service.port.out;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.RegisterVehicleAck;

import java.io.BufferedWriter;
import java.io.IOException;

public interface VehicleGatewayPort {
    void attach(String vehicleId, BufferedWriter writer);

    void detach(String vehicleId);

    void sendAck(String vehicleId, RegisterVehicleAck ack) throws IOException;

    void sendEnvironment(String vehicleId, EnvironmentUpdate update) throws IOException;

    void sendControlCommand(String vehicleId, ControlCommand command) throws IOException;
}

