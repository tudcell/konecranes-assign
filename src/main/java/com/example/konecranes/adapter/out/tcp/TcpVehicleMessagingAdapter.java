package com.example.konecranes.adapter.out.tcp;

import com.example.konecranes.application.port.out.VehicleCommandGatewayPort;
import com.example.konecranes.application.port.out.VehicleEnvironmentGatewayPort;
import com.example.konecranes.application.port.out.VehicleRegistrationGatewayPort;
import com.example.konecranes.application.port.out.VehicleSessionChannel;
import com.example.konecranes.application.port.out.VehicleSessionRegistryPort;
import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.MessageType;
import com.example.konecranes.messaging.RegisterVehicleAck;
import com.example.konecranes.messaging.WireMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outbound TCP adapter that stores active vehicle session channels
 * and sends wire messages to connected vehicle processes.
 */
@Service
public class TcpVehicleMessagingAdapter implements VehicleRegistrationGatewayPort,
        VehicleEnvironmentGatewayPort,
        VehicleCommandGatewayPort,
        VehicleSessionRegistryPort {

    private static final Logger logger = LoggerFactory.getLogger(TcpVehicleMessagingAdapter.class);

    private final Map<String, VehicleSessionChannel> channels = new ConcurrentHashMap<>();

    @Override
    public void attach(String vehicleId, VehicleSessionChannel channel) {
        channels.put(vehicleId, channel);
    }

    @Override
    public void detach(String vehicleId) {
        VehicleSessionChannel channel = channels.remove(vehicleId);
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (IOException ex) {
            logger.debug("Ignoring channel close failure for {}", vehicleId, ex);
        }
    }

    @Override
    public void detachAll() {
        for (String vehicleId : new ArrayList<>(channels.keySet())) {
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
        VehicleSessionChannel channel = channels.get(vehicleId);
        if (channel == null) {
            throw new IOException("No active connection for vehicle " + vehicleId);
        }
        channel.send(message);
    }
}