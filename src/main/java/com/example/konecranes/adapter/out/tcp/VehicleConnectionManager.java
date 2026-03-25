package com.example.konecranes.adapter.out.tcp;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.RegisterVehicleAck;
import com.example.konecranes.messaging.WireMessage;
import com.example.konecranes.model.MessageType;
import com.example.konecranes.application.port.out.VehicleCommandGatewayPort;
import com.example.konecranes.application.port.out.VehicleEnvironmentGatewayPort;
import com.example.konecranes.application.port.out.VehicleRegistrationGatewayPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VehicleConnectionManager implements VehicleRegistrationGatewayPort,
        VehicleEnvironmentGatewayPort,
        VehicleCommandGatewayPort,
        VehicleSessionConnectionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(VehicleConnectionManager.class);

    private final ObjectMapper objectMapper;
    private final Map<String, BufferedWriter> writers = new ConcurrentHashMap<>();

    public VehicleConnectionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void attach(String vehicleId, BufferedWriter writer) {
        writers.put(vehicleId, writer);
    }

    @Override
    public void detach(String vehicleId) {
        BufferedWriter writer = writers.remove(vehicleId);
        if (writer == null) {
            return;
        }
        try {
            writer.close();
        } catch (IOException ex) {
            logger.debug("Ignoring writer close failure for {}", vehicleId, ex);
        }
    }

    @Override
    public void detachAll() {
        // Create a copy to avoid ConcurrentModificationException
        for (String vehicleId : new ArrayList<>(writers.keySet())) {
            detach(vehicleId);
        }
    }

    @Override
    public void sendAck(String vehicleId, RegisterVehicleAck ack) throws IOException {
        send(vehicleId, new WireMessage(MessageType.REGISTER_ACK, ack));
    }

    @Override
    public void sendEnvironment(String vehicleId, EnvironmentUpdate update) throws IOException {
        send(vehicleId, new WireMessage(MessageType.ENVIRONMENT_UPDATE, update));
    }

    @Override
    public void sendControlCommand(String vehicleId, ControlCommand command) throws IOException {
        send(vehicleId, new WireMessage(MessageType.CONTROL_COMMAND, command));
    }

    private void send(String vehicleId, WireMessage message) throws IOException {
        BufferedWriter writer = writers.get(vehicleId);
        if (writer == null) {
            throw new IOException("No active connection for vehicle " + vehicleId);
        }
        synchronized (writer) {
            writer.write(objectMapper.writeValueAsString(message));
            writer.newLine();
            writer.flush();
        }
    }
}



