package com.example.konecranes.vehicle;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.messaging.WireMessage;
import com.example.konecranes.model.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class VehicleProcessRuntime {

    private static final Logger logger = LoggerFactory.getLogger(VehicleProcessRuntime.class);

    private final VehicleProcessConfig config;
    private final VehicleBehaviorEngine behaviorEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BlockingQueue<WireMessage> outboundQueue = new LinkedBlockingQueue<>();

    public VehicleProcessRuntime(VehicleProcessConfig config) {
        this.config = config;
        this.behaviorEngine = new VehicleBehaviorEngine(config);
    }

    public void start() {
        int reconnectAttempt = 0;
        while (!Thread.currentThread().isInterrupted()) {
            SessionOutcome outcome = runSingleSession();
            if (outcome == SessionOutcome.DISCONNECTED_AFTER_CONNECT) {
                reconnectAttempt = 0;
            }

            if (reconnectAttempt >= config.getReconnectMaxAttempts()) {
                logger.warn("Vehicle {} reached reconnect limit ({}). Exiting process.",
                        config.getVehicleId(), config.getReconnectMaxAttempts());
                return;
            }

            long backoffMillis = computeBackoffMillis(reconnectAttempt);
            reconnectAttempt++;
            if (!sleepBeforeReconnect(backoffMillis)) {
                return;
            }
            logger.info("Vehicle {} reconnecting now (attempt {}/{})",
                    config.getVehicleId(),
                    reconnectAttempt,
                    config.getReconnectMaxAttempts());
        }
    }

    private SessionOutcome runSingleSession() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        Thread writerThread = null;
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicReference<Exception> writerFailure = new AtomicReference<>();
        outboundQueue.clear();
        try (Socket socket = new Socket(config.getGatewayHost(), config.getGatewayPort());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            register();
            writerThread = new Thread(() -> writerLoop(writer, socket, running, writerFailure), "vehicle-writer-" + config.getVehicleId());
            writerThread.setDaemon(true);
            writerThread.start();

            long aiTickMillis = config.getTickMillis() > 0 ? Math.max(config.getTickMillis(), 50L) : 150L;
            executor.scheduleAtFixedRate(behaviorEngine::movementTick, 0L, config.getTickMillis(), TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(behaviorEngine::aiTick, 0L, aiTickMillis, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(this::publishState, 0L, config.getTickMillis(), TimeUnit.MILLISECONDS);

            String line;
            while (running.get() && (line = reader.readLine()) != null) {
                handleIncoming(line);
            }

            if (writerFailure.get() != null) {
                logger.warn("Vehicle {} writer loop failed; reconnecting", config.getVehicleId(), writerFailure.get());
                return SessionOutcome.DISCONNECTED_AFTER_CONNECT;
            }

            logger.info("Vehicle {} connection closed by gateway", config.getVehicleId());
            return SessionOutcome.DISCONNECTED_AFTER_CONNECT;
        } catch (SocketException ex) {
            logger.info("Vehicle {} connection reset; reconnecting", config.getVehicleId());
            return SessionOutcome.CONNECT_FAILURE;
        } catch (IOException ex) {
            logger.warn("Vehicle {} I/O failure; reconnecting", config.getVehicleId(), ex);
            return SessionOutcome.CONNECT_FAILURE;
        } finally {
            running.set(false);
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

    private void writerLoop(BufferedWriter writer,
                            Socket socket,
                            AtomicBoolean running,
                            AtomicReference<Exception> writerFailure) {
        try {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                WireMessage message = outboundQueue.poll(250L, TimeUnit.MILLISECONDS);
                if (message == null) {
                    continue;
                }
                writer.write(objectMapper.writeValueAsString(message));
                writer.newLine();
                writer.flush();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            writerFailure.compareAndSet(null, e);
            running.set(false);
            closeQuietly(socket);
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

    private long computeBackoffMillis(int reconnectAttempt) {
        long initial = Math.max(1L, config.getReconnectInitialBackoffMillis());
        long max = Math.max(initial, config.getReconnectMaxBackoffMillis());
        long factor = 1L << Math.min(reconnectAttempt, 20);
        long backoff = initial * factor;
        return Math.min(backoff, max);
    }

    private void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
            // Best-effort close to unblock the reader when writer fails.
        }
    }

    private boolean sleepBeforeReconnect(long backoffMillis) {
        try {
            Thread.sleep(backoffMillis);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private enum SessionOutcome {
        CONNECT_FAILURE,
        DISCONNECTED_AFTER_CONNECT
    }
}
