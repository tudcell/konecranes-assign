package com.example.konecranes.messaging.gateway;

import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.messaging.WireMessage;
import com.example.konecranes.model.MessageType;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.service.VehicleSessionService;
import com.example.konecranes.service.VehicleUpdateService;
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
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class VehicleSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(VehicleSessionHandler.class);

    private final ObjectMapper objectMapper;
    private final VehicleSessionService vehicleSessionService;
    private final VehicleUpdateService vehicleUpdateService;

    public VehicleSessionHandler(ObjectMapper objectMapper,
                                 VehicleSessionService vehicleSessionService,
                                 VehicleUpdateService vehicleUpdateService) {
        this.objectMapper = objectMapper;
        this.vehicleSessionService = vehicleSessionService;
        this.vehicleUpdateService = vehicleUpdateService;
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
                    if (message.getType() == MessageType.REGISTER) {
                        RegisterVehicleRequest request = objectMapper.convertValue(message.getPayload(), RegisterVehicleRequest.class);
                        vehicleId = request.getVehicleId();
                        vehicleSessionService.register(request, writer);
                    } else if (message.getType() == MessageType.STATE_UPDATE) {
                        VehicleState state = objectMapper.convertValue(message.getPayload(), VehicleState.class);
                        vehicleId = state.getId();
                        vehicleUpdateService.updateState(state);
                    } else if (message.getType() == MessageType.DISCONNECT) {
                        Map<String, String> payload = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
                        });
                        vehicleId = payload.get("vehicleId");
                        break;
                    } else {
                        logger.debug("Ignoring unsupported message type {}", message.getType());
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
            logger.warn("Vehicle session I/O failed for {}", vehicleId == null ? "unknown" : vehicleId, ex);
        } finally {
            if (vehicleId != null) {
                vehicleSessionService.disconnect(vehicleId);
            }
        }
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

