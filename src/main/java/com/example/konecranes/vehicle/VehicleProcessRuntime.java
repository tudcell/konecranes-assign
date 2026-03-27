package com.example.konecranes.vehicle;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.MessageType;
import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.messaging.WireMessage;
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

/**
 * Runtime loop for one spawned vehicle process.
 *
 * Responsible for:
 * - opening and maintaining the TCP session
 * - starting periodic movement and AI ticks
 * - publishing state updates
 * - reconnecting when the connection is lost
 */
public class VehicleProcessRuntime {

    private static final Logger logger = LoggerFactory.getLogger(VehicleProcessRuntime.class);

    private final VehicleProcessConfig vehicleProcessConfig;
    private final VehicleBehaviorEngine vehicleBehaviorEngine;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final BlockingQueue<WireMessage> outboundMessageQueue = new LinkedBlockingQueue<>();

    public VehicleProcessRuntime(VehicleProcessConfig vehicleProcessConfig) {
        this.vehicleProcessConfig = vehicleProcessConfig;
        this.vehicleBehaviorEngine = new VehicleBehaviorEngine(vehicleProcessConfig);
    }

    /**
     * Starts the vehicle runtime loop.
     *
     * Reconnects until the configured reconnect limit is reached
     * or the current thread is interrupted.
     */
    public void start() {
        int reconnectAttempt = 0;

        while (!Thread.currentThread().isInterrupted()) {
            SessionOutcome sessionOutcome = runSingleSession();

            if (sessionOutcome == SessionOutcome.DISCONNECTED_AFTER_CONNECT) {
                reconnectAttempt = 0;
            }

            if (reconnectAttempt >= vehicleProcessConfig.getReconnectMaxAttempts()) {
                logger.warn(
                        "Vehicle {} reached reconnect limit ({}). Exiting process.",
                        vehicleProcessConfig.getVehicleId(),
                        vehicleProcessConfig.getReconnectMaxAttempts()
                );
                return;
            }

            long reconnectBackoffMillis = computeBackoffMillis(reconnectAttempt);
            reconnectAttempt++;

            if (!sleepBeforeReconnect(reconnectBackoffMillis)) {
                return;
            }

            logger.info(
                    "Vehicle {} reconnecting now (attempt {}/{})",
                    vehicleProcessConfig.getVehicleId(),
                    reconnectAttempt,
                    vehicleProcessConfig.getReconnectMaxAttempts()
            );
        }
    }

    /**
     * Runs one full TCP session from connect until disconnect.
     *
     * Creates:
     * - one writer thread for outbound messages
     * - one scheduled executor for movement, AI, and state publishing
     *
     * @return session outcome used by reconnect logic
     */
    private SessionOutcome runSingleSession() {
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(3);
        Thread writerThread = null;
        AtomicBoolean sessionRunning = new AtomicBoolean(true);
        AtomicReference<Exception> writerFailureRef = new AtomicReference<>();

        outboundMessageQueue.clear();

        try (Socket socket = new Socket(vehicleProcessConfig.getGatewayHost(), vehicleProcessConfig.getGatewayPort());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            register();

            writerThread = new Thread(
                    () -> writerLoop(writer, socket, sessionRunning, writerFailureRef),
                    "vehicle-writer-" + vehicleProcessConfig.getVehicleId()
            );
            writerThread.setDaemon(true);
            writerThread.start();

            long aiTickMillis = vehicleProcessConfig.getTickMillis() > 0
                    ? Math.max(vehicleProcessConfig.getTickMillis(), 50L)
                    : 150L;

            scheduledExecutor.scheduleAtFixedRate(
                    vehicleBehaviorEngine::movementTick,
                    0L,
                    vehicleProcessConfig.getTickMillis(),
                    TimeUnit.MILLISECONDS
            );
            scheduledExecutor.scheduleAtFixedRate(
                    vehicleBehaviorEngine::aiTick,
                    0L,
                    aiTickMillis,
                    TimeUnit.MILLISECONDS
            );
            scheduledExecutor.scheduleAtFixedRate(
                    this::publishState,
                    0L,
                    vehicleProcessConfig.getTickMillis(),
                    TimeUnit.MILLISECONDS
            );

            String incomingLine;
            while (sessionRunning.get() && (incomingLine = reader.readLine()) != null) {
                handleIncoming(incomingLine);
            }

            if (writerFailureRef.get() != null) {
                logger.warn(
                        "Vehicle {} writer loop failed; reconnecting",
                        vehicleProcessConfig.getVehicleId(),
                        writerFailureRef.get()
                );
                return SessionOutcome.DISCONNECTED_AFTER_CONNECT;
            }

            logger.info("Vehicle {} connection closed by gateway", vehicleProcessConfig.getVehicleId());
            return SessionOutcome.DISCONNECTED_AFTER_CONNECT;

        } catch (SocketException ex) {
            logger.info("Vehicle {} connection reset; reconnecting", vehicleProcessConfig.getVehicleId());
            return SessionOutcome.CONNECT_FAILURE;

        } catch (IOException ex) {
            logger.warn("Vehicle {} I/O failure; reconnecting", vehicleProcessConfig.getVehicleId(), ex);
            return SessionOutcome.CONNECT_FAILURE;

        } finally {
            sessionRunning.set(false);
            scheduledExecutor.shutdownNow();

            if (writerThread != null) {
                writerThread.interrupt();
            }
        }
    }

    /**
     * Enqueues the initial REGISTER message for a new connection.
     */
    private void register() {
        RegisterVehicleRequest registerVehicleRequest = new RegisterVehicleRequest();
        registerVehicleRequest.setVehicleId(vehicleProcessConfig.getVehicleId());
        registerVehicleRequest.setInitialX(vehicleProcessConfig.getInitialX());
        registerVehicleRequest.setInitialY(vehicleProcessConfig.getInitialY());
        registerVehicleRequest.setInitialDirectionDeg(vehicleProcessConfig.getInitialDirectionDeg());
        registerVehicleRequest.setInitialSpeed(vehicleProcessConfig.getInitialSpeed());
        registerVehicleRequest.setRadius(vehicleProcessConfig.getRadius());

        outboundMessageQueue.add(new WireMessage(MessageType.REGISTER, registerVehicleRequest));
    }

    /**
     * Writes outbound messages from the queue to the socket.
     *
     * Stops when:
     * - runtime is no longer running
     * - the thread is interrupted
     * - socket writing fails
     *
     * On write failure, the socket is closed to unblock the reader side.
     *
     * @param writer socket writer
     * @param socket socket associated with the writer
     * @param sessionRunning shared running flag
     * @param writerFailureRef stores the first write failure
     */
    private void writerLoop(BufferedWriter writer,
                            Socket socket,
                            AtomicBoolean sessionRunning,
                            AtomicReference<Exception> writerFailureRef) {
        try {
            while (sessionRunning.get() && !Thread.currentThread().isInterrupted()) {
                WireMessage wireMessage = outboundMessageQueue.poll(250L, TimeUnit.MILLISECONDS);
                if (wireMessage == null) {
                    continue;
                }

                writer.write(jsonMapper.writeValueAsString(wireMessage));
                writer.newLine();
                writer.flush();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            writerFailureRef.compareAndSet(null, ex);
            sessionRunning.set(false);
            closeQuietly(socket);
        }
    }

    /**
     * Parses one inbound wire message and dispatches it
     * to the behavior engine.
     *
     * Supported message types:
     * - ENVIRONMENT_UPDATE
     * - CONTROL_COMMAND
     *
     * @param incomingLine raw JSON message line
     * @throws IOException when parsing fails
     */
    private void handleIncoming(String incomingLine) throws IOException {
        WireMessage wireMessage = jsonMapper.readValue(incomingLine, WireMessage.class);

        if (wireMessage.getType() == MessageType.ENVIRONMENT_UPDATE) {
            EnvironmentUpdate environmentUpdate =
                    jsonMapper.convertValue(wireMessage.getPayload(), EnvironmentUpdate.class);
            vehicleBehaviorEngine.onEnvironmentUpdate(environmentUpdate);
        } else if (wireMessage.getType() == MessageType.CONTROL_COMMAND) {
            ControlCommand controlCommand =
                    jsonMapper.convertValue(wireMessage.getPayload(), ControlCommand.class);
            vehicleBehaviorEngine.onControlCommand(controlCommand);
        }
    }

    /**
     * Enqueues one STATE_UPDATE message using the current vehicle state.
     */
    private void publishState() {
        outboundMessageQueue.add(new WireMessage(
                MessageType.STATE_UPDATE,
                vehicleBehaviorEngine.currentStateCopy()
        ));
    }

    /**
     * Computes reconnect backoff using exponential growth
     * clamped by configured min and max values.
     *
     * @param reconnectAttempt zero-based reconnect attempt number
     * @return backoff time in milliseconds
     */
    private long computeBackoffMillis(int reconnectAttempt) {
        long initialBackoffMillis = Math.max(1L, vehicleProcessConfig.getReconnectInitialBackoffMillis());
        long maxBackoffMillis = Math.max(initialBackoffMillis, vehicleProcessConfig.getReconnectMaxBackoffMillis());
        long backoffFactor = 1L << Math.min(reconnectAttempt, 20);
        long computedBackoffMillis = initialBackoffMillis * backoffFactor;
        return Math.min(computedBackoffMillis, maxBackoffMillis);
    }

    /**
     * Closes a socket without throwing.
     *
     * Used as best-effort cleanup when writer-side failures occur.
     *
     * @param socket socket to close
     */
    private void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
            // Best-effort close to unblock the reader when writer fails.
        }
    }

    /**
     * Sleeps before the next reconnect attempt.
     *
     * @param reconnectBackoffMillis sleep duration in milliseconds
     * @return true when sleep completed normally, false when interrupted
     */
    private boolean sleepBeforeReconnect(long reconnectBackoffMillis) {
        try {
            Thread.sleep(reconnectBackoffMillis);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Outcome of one completed session attempt.
     */
    private enum SessionOutcome {
        CONNECT_FAILURE,
        DISCONNECTED_AFTER_CONNECT
    }
}