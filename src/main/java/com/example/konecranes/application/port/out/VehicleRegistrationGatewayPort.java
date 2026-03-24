package com.example.konecranes.application.port.out;

import com.example.konecranes.messaging.RegisterVehicleAck;

import java.io.IOException;

public interface VehicleRegistrationGatewayPort {
    void sendAck(String vehicleId, RegisterVehicleAck ack) throws IOException;
}


