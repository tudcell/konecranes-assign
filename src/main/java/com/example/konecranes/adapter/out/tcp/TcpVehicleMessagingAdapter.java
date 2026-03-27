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
 * TCP outbound adapter responsible for:
 * - storing active vehicle session channels
 * - sending wire messages to connected vehicle processes
 *
 * This adapter implements both:
 * - messaging ports used by the application layer
 * - session registry port used to track active TCP channels
 */
@Service
public class TcpVehicleMessagingAdapter implements VehicleRegistrationGatewayPort,
        VehicleEnvironmentGatewayPort,
        VehicleCommandGatewayPort,
        VehicleSessionRegistryPort {

    private static final Logger logger = LoggerFactory.getLogger(TcpVehicleMessagingAdapter.class);

    private final Map<String, VehicleSessionChannel> vehicleChannels = new ConcurrentHashMap<>();

    /**
     * Registers one active channel for a vehicle id.
     *
     * @param vehicleId connected vehicle id
     * @param channel active session channel
     */
    @Override
    public void attach(String vehicleId, VehicleSessionChannel channel) {
        vehicleChannels.put(vehicleId, channel);
    }

    /**
     * Removes and closes the active channel for one vehicle.
     *
     * @param vehicleId vehicle id to detach
     */
    @Override
    public void detach(String vehicleId) {
        VehicleSessionChannel channel = vehicleChannels.remove(vehicleId);
        if (channel == null) {
            return;
        }

        try {
            channel.close();
        } catch (IOException ex) {
            logger.debug("Ignoring channel close failure for {}", vehicleId, ex);
        }
    }

    /**
     * Removes and closes all active vehicle channels.
     */
    @Override
    public void detachAll() {
        for (String vehicleId : new ArrayList<>(vehicleChannels.keySet())) {
            detach(vehicleId);
        }
    }

    /**
     * Sends registration acknowledgement to one vehicle process.
     *
     * @param vehicleId target vehicle id
     * @param ack registration acknowledgement payload
     * @throws IOException when send fails or no active channel exists
     */
    @Override
    public void sendAck(String vehicleId, RegisterVehicleAck ack) throws IOException {
        send(vehicleId, new WireMessage(MessageType.REGISTER_ACK, ack));
    }

    /**
     * Sends environment update to one vehicle process.
     *
     * @param vehicleId target vehicle id
     * @param update environment update payload
     * @throws IOException when send fails or no active channel exists
     */
    @Override
    public void sendEnvironment(String vehicleId, EnvironmentUpdate update) throws IOException {
        send(vehicleId, new WireMessage(MessageType.ENVIRONMENT_UPDATE, update));
    }

    /**
     * Sends manual control command to one vehicle process.
     *
     * @param vehicleId target vehicle id
     * @param command manual control command payload
     * @throws IOException when send fails or no active channel exists
     */
    @Override
    public void sendControlCommand(String vehicleId, ControlCommand command) throws IOException {
        send(vehicleId, new WireMessage(MessageType.CONTROL_COMMAND, command));
    }

    /**
     * Sends one wire message through the active channel of a vehicle.
     *
     * @param vehicleId target vehicle id
     * @param message message to send
     * @throws IOException when no active channel exists or transport send fails
     */
    private void send(String vehicleId, WireMessage message) throws IOException {
        VehicleSessionChannel channel = vehicleChannels.get(vehicleId);
        if (channel == null) {
            throw new IOException("No active connection for vehicle " + vehicleId);
        }
        channel.send(message);
    }
}