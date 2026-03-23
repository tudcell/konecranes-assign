package com.example.konecranes.messaging.gateway;

import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.messaging.WireMessage;
import com.example.konecranes.model.MessageType;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.service.VehicleSessionService;
import com.example.konecranes.service.VehicleUpdateService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                WireMessage message = objectMapper.readValue(line, WireMessage.class);
                if (message.getType() == MessageType.REGISTER) {
                    RegisterVehicleRequest request = objectMapper.convertValue(message.getPayload(), RegisterVehicleRequest.class);
                    vehicleId = request.getVehicleId();
                    vehicleSessionService.register(request, writer);
                } else if (message.getType() == MessageType.STATE_UPDATE) {
                    VehicleState state = objectMapper.convertValue(message.getPayload(), VehicleState.class);
                    vehicleId = state.getId();
                    vehicleUpdateService.updateState(state);
                } else if (message.getType() == MessageType.DISCONNECT) {
                    Map<String, String> payload = objectMapper.convertValue(message.getPayload(), new TypeReference<Map<String, String>>() {
                    });
                    vehicleId = payload.get("vehicleId");
                    break;
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (vehicleId != null) {
                vehicleSessionService.disconnect(vehicleId);
            }
        }
    }
}

