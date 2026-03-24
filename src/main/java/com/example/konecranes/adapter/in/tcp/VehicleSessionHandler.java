package com.example.konecranes.adapter.in.tcp;

import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.messaging.WireMessage;
import com.example.konecranes.model.MessageType;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.application.port.in.DisconnectVehicleSessionCommand;
import com.example.konecranes.application.port.in.DisconnectVehicleSessionUseCase;
import com.example.konecranes.application.port.in.RegisterVehicleSessionCommand;
import com.example.konecranes.application.port.in.RegisterVehicleSessionUseCase;
import com.example.konecranes.application.port.in.UpdateVehicleStateCommand;
import com.example.konecranes.application.port.in.UpdateVehicleStateUseCase;
import com.example.konecranes.adapter.out.tcp.VehicleSessionConnectionRegistry;
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

@Service
public class VehicleSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(VehicleSessionHandler.class);

    private final ObjectMapper objectMapper;
    private final RegisterVehicleSessionUseCase registerVehicleSessionUseCase;
    private final DisconnectVehicleSessionUseCase disconnectVehicleSessionUseCase;
    private final UpdateVehicleStateUseCase updateVehicleStateUseCase;
    private final VehicleSessionConnectionRegistry sessionConnectionRegistry;

    public VehicleSessionHandler(ObjectMapper objectMapper,
                                 RegisterVehicleSessionUseCase registerVehicleSessionUseCase,
                                 DisconnectVehicleSessionUseCase disconnectVehicleSessionUseCase,
                                 UpdateVehicleStateUseCase updateVehicleStateUseCase,
                                 VehicleSessionConnectionRegistry sessionConnectionRegistry) {
        this.objectMapper = objectMapper;
        this.registerVehicleSessionUseCase = registerVehicleSessionUseCase;
        this.disconnectVehicleSessionUseCase = disconnectVehicleSessionUseCase;
        this.updateVehicleStateUseCase = updateVehicleStateUseCase;
        this.sessionConnectionRegistry = sessionConnectionRegistry;
    }

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
                logger.info("Vehicle session closed for {}", resolvedVehicleId);
            } else {
                logger.warn("Vehicle session I/O failed for {}", resolvedVehicleId, ex);
            }
        } finally {
            if (vehicleId != null) {
                sessionConnectionRegistry.detach(vehicleId);
                disconnectVehicleSessionUseCase.disconnect(new DisconnectVehicleSessionCommand(vehicleId));
            }
        }
    }

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

    private String handleRegister(WireMessage message, BufferedWriter writer) throws IOException {
        RegisterVehicleRequest request = objectMapper.convertValue(message.getPayload(), RegisterVehicleRequest.class);
        sessionConnectionRegistry.attach(request.getVehicleId(), writer);
        try {
            RegisterVehicleSessionCommand command = new RegisterVehicleSessionCommand(
                    request.getVehicleId(),
                    request.getInitialX(),
                    request.getInitialY(),
                    request.getInitialDirectionDeg(),
                    request.getInitialSpeed(),
                    request.getRadius());
            registerVehicleSessionUseCase.register(command);
        } catch (IOException ex) {
            sessionConnectionRegistry.detach(request.getVehicleId());
            throw ex;
        }
        return request.getVehicleId();
    }

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
                state.getCurrentRiskScore());
        updateVehicleStateUseCase.updateState(command);
        return state.getId();
    }

    private String handleDisconnect(WireMessage message) {
        Map<String, String> payload = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
        });
        return payload.get("vehicleId");
    }

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
