package com.example.konecranes.service;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.MessageType;
import com.example.konecranes.messaging.RegisterVehicleAck;
import com.example.konecranes.messaging.WireMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VehicleConnectionManager {

    private final ObjectMapper objectMapper;
    private final Map<String, BufferedWriter> writers = new ConcurrentHashMap<>();

    public VehicleConnectionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void attach(String vehicleId, BufferedWriter writer) {
        writers.put(vehicleId, writer);
    }

    public void detach(String vehicleId) {
        writers.remove(vehicleId);
    }

    public void sendAck(String vehicleId, RegisterVehicleAck ack) throws IOException {
        send(vehicleId, new WireMessage(MessageType.REGISTER_ACK, ack));
    }

    public void sendEnvironment(String vehicleId, EnvironmentUpdate update) throws IOException {
        send(vehicleId, new WireMessage(MessageType.ENVIRONMENT_UPDATE, update));
    }

    public void sendControlCommand(String vehicleId, ControlCommand command) throws IOException {
        send(vehicleId, new WireMessage(MessageType.CONTROL_COMMAND, command));
    }

    private void send(String vehicleId, WireMessage message) throws IOException {
        BufferedWriter writer = writers.get(vehicleId);
        if (writer == null) {
            return;
        }
        synchronized (writer) {
            writer.write(objectMapper.writeValueAsString(message));
            writer.newLine();
            writer.flush();
        }
    }
}
