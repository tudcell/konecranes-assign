package com.example.konecranes.adapter.out.tcp;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.RegisterVehicleAck;
import com.example.konecranes.messaging.WireMessage;
import com.example.konecranes.messaging.MessageType;
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

/**
 * Outbound TCP adapter that stores active vehicle writers and sends wire messages.
 */
@Service
public class TcpVehicleGatewayAdapter implements VehicleRegistrationGatewayPort,
        VehicleEnvironmentGatewayPort,
        VehicleCommandGatewayPort,
        SessionConnectionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(TcpVehicleGatewayAdapter.class);

    private final ObjectMapper objectMapper;
    private final Map<String, BufferedWriter> writers = new ConcurrentHashMap<>();

    public TcpVehicleGatewayAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Registers a writer for one active vehicle session.
     */
    @Override
    public void attach(String vehicleId, BufferedWriter writer) {
        writers.put(vehicleId, writer);
    }

    /**
     * Removes and closes a writer for one vehicle session.
     */
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

    /**
     * Detaches all active vehicle writers simultaneously.
     */
    @Override
    public void detachAll() {
        // Create a copy to avoid ConcurrentModificationException
        for (String vehicleId : new ArrayList<>(writers.keySet())) {
            detach(vehicleId);
        }
    }

    /**
     * Sends registration acknowledgement to one vehicle process.
     */
    @Override
    public void sendAck(String vehicleId, RegisterVehicleAck ack) throws IOException {
        send(vehicleId, new WireMessage(MessageType.REGISTER_ACK, ack));
    }

    /**
     * Sends environment update to one vehicle process.
     */
    @Override
    public void sendEnvironment(String vehicleId, EnvironmentUpdate update) throws IOException {
        send(vehicleId, new WireMessage(MessageType.ENVIRONMENT_UPDATE, update));
    }

    /**
     * Sends manual control command to one vehicle process.
     */
    @Override
    public void sendControlCommand(String vehicleId, ControlCommand command) throws IOException {
        send(vehicleId, new WireMessage(MessageType.CONTROL_COMMAND, command));
    }

    /**
     * Sends a wire message to one vehicle via its registered writer.
     *
     * @param vehicleId target vehicle id
     * @param message wire message to send
     * @throws IOException when vehicle writer is not registered or send fails
     */
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



