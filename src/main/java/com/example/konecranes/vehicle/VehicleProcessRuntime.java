package com.example.konecranes.vehicle;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.messaging.WireMessage;
import com.example.konecranes.model.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VehicleProcessRuntime {

    private final VehicleProcessConfig config;
    private final VehicleBehaviorEngine behaviorEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BlockingQueue<WireMessage> outboundQueue = new LinkedBlockingQueue<>();

    public VehicleProcessRuntime(VehicleProcessConfig config) {
        this.config = config;
        this.behaviorEngine = new VehicleBehaviorEngine(config);
    }

    public void start() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        Thread writerThread = null;
        try (Socket socket = new Socket(config.getGatewayHost(), config.getGatewayPort());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            register();
            writerThread = new Thread(() -> writerLoop(writer), "vehicle-writer-" + config.getVehicleId());
            writerThread.setDaemon(true);
            writerThread.start();

            executor.scheduleAtFixedRate(behaviorEngine::movementTick, 0L, config.getTickMillis(), TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(behaviorEngine::aiTick, 0L, 150L, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(this::publishState, 0L, config.getTickMillis(), TimeUnit.MILLISECONDS);

            String line;
            while ((line = reader.readLine()) != null) {
                handleIncoming(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Vehicle process failed for " + config.getVehicleId(), e);
        } finally {
            executor.shutdownNow();
            if (writerThread != null) {
                writerThread.interrupt();
            }
        }
    }

    private void register() {
        RegisterVehicleRequest request = new RegisterVehicleRequest();
        request.setVehicleId(config.getVehicleId());
        request.setInitialX(config.getInitialX());
        request.setInitialY(config.getInitialY());
        request.setInitialDirectionDeg(config.getInitialDirectionDeg());
        request.setInitialSpeed(config.getInitialSpeed());
        request.setRadius(config.getRadius());
        outboundQueue.add(new WireMessage(MessageType.REGISTER, request));
    }

    private void writerLoop(BufferedWriter writer) {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                WireMessage message = outboundQueue.take();
                writer.write(objectMapper.writeValueAsString(message));
                writer.newLine();
                writer.flush();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new IllegalStateException("Vehicle writer failed", e);
        }
    }

    private void handleIncoming(String line) throws IOException {
        WireMessage message = objectMapper.readValue(line, WireMessage.class);
        if (message.getType() == MessageType.ENVIRONMENT_UPDATE) {
            EnvironmentUpdate update = objectMapper.convertValue(message.getPayload(), EnvironmentUpdate.class);
            behaviorEngine.onEnvironmentUpdate(update);
        } else if (message.getType() == MessageType.CONTROL_COMMAND) {
            ControlCommand command = objectMapper.convertValue(message.getPayload(), ControlCommand.class);
            behaviorEngine.onControlCommand(command);
        }
    }

    private void publishState() {
        outboundQueue.add(new WireMessage(MessageType.STATE_UPDATE, behaviorEngine.currentStateCopy()));
    }
}
