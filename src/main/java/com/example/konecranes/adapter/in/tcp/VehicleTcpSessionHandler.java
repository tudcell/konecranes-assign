package com.example.konecranes.adapter.in.tcp;

import com.example.konecranes.adapter.out.tcp.TcpVehicleSessionChannel;
import com.example.konecranes.application.port.in.DisconnectVehicleSessionCommand;
import com.example.konecranes.application.port.in.DisconnectVehicleSessionUseCase;
import com.example.konecranes.application.port.in.RegisterVehicleSessionCommand;
import com.example.konecranes.application.port.in.RegisterVehicleSessionUseCase;
import com.example.konecranes.application.port.in.UpdateVehicleStateCommand;
import com.example.konecranes.application.port.in.UpdateVehicleStateUseCase;
import com.example.konecranes.application.port.out.VehicleSessionRegistryPort;
import com.example.konecranes.messaging.MessageType;
import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.messaging.WireMessage;
import com.example.konecranes.model.VehicleState;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handles one TCP vehicle session.
 *
 * Reads incoming wire messages from the socket and dispatches them
 * to the correct application use case.
 */
@Service
public class VehicleTcpSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(VehicleTcpSessionHandler.class);

    private final ObjectMapper objectMapper;
    private final RegisterVehicleSessionUseCase registerVehicleSessionUseCase;
    private final DisconnectVehicleSessionUseCase disconnectVehicleSessionUseCase;
    private final UpdateVehicleStateUseCase updateVehicleStateUseCase;
    private final VehicleSessionRegistryPort vehicleSessionRegistryPort;

    public VehicleTcpSessionHandler(ObjectMapper objectMapper,
                                    RegisterVehicleSessionUseCase registerVehicleSessionUseCase,
                                    DisconnectVehicleSessionUseCase disconnectVehicleSessionUseCase,
                                    UpdateVehicleStateUseCase updateVehicleStateUseCase,
                                    VehicleSessionRegistryPort vehicleSessionRegistryPort) {
        this.objectMapper = objectMapper;
        this.registerVehicleSessionUseCase = registerVehicleSessionUseCase;
        this.disconnectVehicleSessionUseCase = disconnectVehicleSessionUseCase;
        this.updateVehicleStateUseCase = updateVehicleStateUseCase;
        this.vehicleSessionRegistryPort = vehicleSessionRegistryPort;
    }

    /**
     * Processes one connected vehicle socket until the session ends.
     *
     * Reads line-delimited JSON messages, dispatches them by type,
     * and performs cleanup when the connection is closed.
     *
     * @param socket accepted vehicle socket
     */
    public void handle(Socket socket) {
        String vehicleId = null;

        try (Socket ignored = socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                WireMessage message = null;
                try {
                    message = objectMapper.readValue(line, WireMessage.class);
                    String resolvedVehicleId = handleMessage(message, writer);

                    if (resolvedVehicleId != null) {
                        vehicleId = resolvedVehicleId;
                    }

                    if (message.getType() == MessageType.DISCONNECT) {
                        break;
                    }
                } catch (Exception ex) {
                    logger.warn("Ignoring invalid vehicle message for {} (type={}, payload={})",
                            vehicleId == null ? "unknown" : vehicleId,
                            message == null ? "unparseable" : String.valueOf(message.getType()),
                            abbreviate(line),
                            ex);
                }
            }
        } catch (IOException ex) {
            String resolvedVehicleId = vehicleId == null ? "unknown" : vehicleId;
            if (isExpectedSocketClose(ex)) {
                logger.info("Vehicle TCP session closed for {}", resolvedVehicleId);
            } else {
                logger.warn("Vehicle TCP session I/O failed for {}", resolvedVehicleId, ex);
            }
        } finally {
            if (vehicleId != null) {
                disconnectVehicleSessionUseCase.disconnect(new DisconnectVehicleSessionCommand(vehicleId));
                vehicleSessionRegistryPort.detach(vehicleId);
            }
        }
    }

    /**
     * Dispatches one incoming wire message to the matching handler.
     *
     * @param message parsed wire message
     * @param writer writer bound to the current socket
     * @return resolved vehicle id when available, otherwise null
     * @throws IOException when register handling fails while sending responses
     */
    private String handleMessage(WireMessage message, BufferedWriter writer) throws IOException {
        if (message.getType() == MessageType.REGISTER) {
            return handleRegister(message, writer);
        }
        if (message.getType() == MessageType.STATE_UPDATE) {
            return handleStateUpdate(message);
        }
        if (message.getType() == MessageType.DISCONNECT) {
            return handleDisconnect(message);
        }

        logger.debug("Ignoring unsupported message type {}", message.getType());
        return null;
    }

    /**
     * Handles vehicle registration.
     *
     * Registers the active session channel, then delegates the actual
     * registration logic to the application layer.
     *
     * @param message register message
     * @param writer socket writer for this session
     * @return registered vehicle id
     * @throws IOException when registration flow fails
     */
    private String handleRegister(WireMessage message, BufferedWriter writer) throws IOException {
        RegisterVehicleRequest request = objectMapper.convertValue(message.getPayload(), RegisterVehicleRequest.class);

        vehicleSessionRegistryPort.attach(
                request.getVehicleId(),
                new TcpVehicleSessionChannel(writer, objectMapper)
        );

        try {
            RegisterVehicleSessionCommand command = new RegisterVehicleSessionCommand(
                    request.getVehicleId(),
                    request.getInitialX(),
                    request.getInitialY(),
                    request.getInitialDirectionDeg(),
                    request.getInitialSpeed(),
                    request.getRadius()
            );
            registerVehicleSessionUseCase.register(command);
        } catch (IOException ex) {
            vehicleSessionRegistryPort.detach(request.getVehicleId());
            throw ex;
        }

        return request.getVehicleId();
    }

    /**
     * Handles a vehicle state update message.
     *
     * Converts the transport payload into an application command
     * and forwards it to the update use case.
     *
     * @param message state update message
     * @return vehicle id contained in the state payload
     */
    private String handleStateUpdate(WireMessage message) {
        VehicleState state = objectMapper.convertValue(message.getPayload(), VehicleState.class);

        UpdateVehicleStateCommand command = new UpdateVehicleStateCommand(
                state.getId(),
                state.getX(),
                state.getY(),
                state.getDirectionDeg(),
                state.getSpeed(),
                state.getRadius(),
                state.getStatus(),
                state.getCurrentAction(),
                state.getRiskLevel(),
                state.getCurrentRiskScore()
        );

        updateVehicleStateUseCase.updateState(command);
        return state.getId();
    }

    /**
     * Handles a disconnect message.
     *
     * @param message disconnect message
     * @return disconnected vehicle id
     */
    private String handleDisconnect(WireMessage message) {
        Map<String, String> payload = objectMapper.convertValue(
                message.getPayload(),
                new TypeReference<>() {}
        );
        return payload.get("vehicleId");
    }

    /**
     * Checks whether the socket failure is an expected connection-close case.
     *
     * @param ex thrown I/O exception
     * @return true when the exception indicates a normal socket close/reset
     */
    private boolean isExpectedSocketClose(IOException ex) {
        if (!(ex instanceof SocketException)) {
            return false;
        }

        String message = ex.getMessage();
        if (message == null) {
            return true;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("socket closed") || normalized.contains("connection reset");
    }

    /**
     * Shortens payload text before logging so logs stay readable.
     *
     * @param payload raw payload text
     * @return abbreviated payload string
     */
    private String abbreviate(String payload) {
        if (payload == null) {
            return "<null>";
        }

        String sanitized = payload.replace("\r", "").replace("\n", "");
        int max = 180;
        if (sanitized.length() <= max) {
            return sanitized;
        }

        return sanitized.substring(0, max) + "...";
    }
}