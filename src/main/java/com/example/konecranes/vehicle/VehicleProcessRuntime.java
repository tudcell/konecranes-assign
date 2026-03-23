package com.example.konecranes.vehicle;

import com.example.konecranes.ai.AvoidanceDecisionEngine;
import com.example.konecranes.ai.RiskEstimator;
import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.messaging.MessageType;
import com.example.konecranes.messaging.RegisterVehicleRequest;
import com.example.konecranes.messaging.WireMessage;
import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class VehicleProcessRuntime {

    private final VehicleProcessConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BlockingQueue<WireMessage> outboundQueue = new LinkedBlockingQueue<>();
    private final ConcurrentMap<String, VehicleState> nearbyVehicles = new ConcurrentHashMap<>();
    private final AtomicReference<VehicleState> selfState = new AtomicReference<>();
    private final AtomicLong targetDirectionDegTimes100 = new AtomicLong(0L);
    private final AtomicBoolean manualOverrideActive = new AtomicBoolean(false);
    private final AtomicLong manualOverrideUntilMillis = new AtomicLong(0L);
    private final AvoidanceDecisionEngine decisionEngine = new AvoidanceDecisionEngine(new RiskEstimator(20, 0.1));
    private static final long MANUAL_OVERRIDE_HOLD_MILLIS = 2000L;
    private static final double COLLISION_GUARD_MARGIN = 8.0;
    private static final double COLLISION_LOOKAHEAD_SECONDS = 0.25;
    private static final double MAX_TURN_DEG_PER_TICK = 12.0;

    public VehicleProcessRuntime(VehicleProcessConfig config) {
        this.config = config;
        VehicleState initial = new VehicleState();
        initial.setId(config.getVehicleId());
        initial.setX(config.getInitialX());
        initial.setY(config.getInitialY());
        initial.setDirectionDeg(config.getInitialDirectionDeg());
        initial.setSpeed(config.getInitialSpeed());
        initial.setRadius(config.getRadius());
        initial.setStatus(VehicleStatus.ACTIVE);
        initial.setTimestamp(System.currentTimeMillis());
        selfState.set(initial);
        setTargetDirection(initial.getDirectionDeg());
    }

    public void start() {
        try (Socket socket = new Socket(config.getGatewayHost(), config.getGatewayPort());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            register();
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
            Thread writerThread = new Thread(() -> writerLoop(writer), "vehicle-writer-" + config.getVehicleId());
            writerThread.setDaemon(true);
            writerThread.start();

            executor.scheduleAtFixedRate(this::movementTick, 0L, config.getTickMillis(), TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(this::aiTick, 0L, 150L, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(this::publishState, 0L, config.getTickMillis(), TimeUnit.MILLISECONDS);

            String line;
            while ((line = reader.readLine()) != null) {
                handleIncoming(line);
            }
            executor.shutdownNow();
        } catch (IOException e) {
            throw new IllegalStateException("Vehicle process failed for " + config.getVehicleId(), e);
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
        outboundQueue.offer(new WireMessage(MessageType.REGISTER, request));
    }

    private void writerLoop(BufferedWriter writer) {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                WireMessage message = outboundQueue.take();
                synchronized (writer) {
                    writer.write(objectMapper.writeValueAsString(message));
                    writer.newLine();
                    writer.flush();
                }
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
            applyEnvironment(update);
        } else if (message.getType() == MessageType.CONTROL_COMMAND) {
            ControlCommand command = objectMapper.convertValue(message.getPayload(), ControlCommand.class);
            applyControlCommand(command);
        }
    }

    private void applyEnvironment(EnvironmentUpdate update) {
        nearbyVehicles.clear();
        for (VehicleState vehicleState : update.getNearbyVehicles()) {
            nearbyVehicles.put(vehicleState.getId(), vehicleState);
        }
    }

    private void applyControlCommand(ControlCommand command) {
        VehicleState state = selfState.get();
        if (command.getOverrideDirectionDeg() != null) {
            setTargetDirection(command.getOverrideDirectionDeg());
        }
        if (command.getOverrideSpeed() != null) {
            state.setSpeed(Math.max(0.0, command.getOverrideSpeed()));
        }
        if (command.isManualOverride()) {
            manualOverrideActive.set(true);
            manualOverrideUntilMillis.set(System.currentTimeMillis() + MANUAL_OVERRIDE_HOLD_MILLIS);
            state.setCurrentAction(AvoidanceAction.USER_OVERRIDE);
        }
    }

    private void movementTick() {
        VehicleState state = selfState.get();
        rotateTowardsTarget(state, MAX_TURN_DEG_PER_TICK);
        double dtSeconds = config.getTickMillis() / 1000.0;
        double directionRad = Math.toRadians(state.getDirectionDeg());
        double nextX = state.getX() + Math.cos(directionRad) * state.getSpeed() * dtSeconds;
        double nextY = state.getY() + Math.sin(directionRad) * state.getSpeed() * dtSeconds;

        VehicleState threat = findImmediateThreat(state, nextX, nextY);
        if (threat != null) {
            applyEmergencyManeuver(state, threat);
            bounceIfNeeded(state);
            state.setTimestamp(System.currentTimeMillis());
            return;
        }

        state.setX(nextX);
        state.setY(nextY);

        bounceIfNeeded(state);
        state.setTimestamp(System.currentTimeMillis());
    }

    private VehicleState findImmediateThreat(VehicleState state, double nextX, double nextY) {
        VehicleState nearestThreat = null;
        double nearestDistance = Double.MAX_VALUE;

        for (VehicleState other : nearbyVehicles.values()) {
            if (other == null || state.getId().equals(other.getId())) {
                continue;
            }

            double nowDistance = distance(state.getX(), state.getY(), other.getX(), other.getY());
            double collisionDistance = state.getRadius() + other.getRadius() + COLLISION_GUARD_MARGIN;
            if (nowDistance < nearestDistance && isCollisionLikely(state, other, nextX, nextY, collisionDistance)) {
                nearestDistance = nowDistance;
                nearestThreat = other;
            }
        }

        return nearestThreat;
    }

    private boolean isCollisionLikely(VehicleState self, VehicleState other, double selfNextX, double selfNextY,
                                      double collisionDistance) {
        double distanceAtNextStep = distance(selfNextX, selfNextY, other.getX(), other.getY());
        if (distanceAtNextStep <= collisionDistance) {
            return true;
        }

        double otherHeadingRad = Math.toRadians(other.getDirectionDeg());
        double otherFutureX = other.getX() + Math.cos(otherHeadingRad) * other.getSpeed() * COLLISION_LOOKAHEAD_SECONDS;
        double otherFutureY = other.getY() + Math.sin(otherHeadingRad) * other.getSpeed() * COLLISION_LOOKAHEAD_SECONDS;

        double selfHeadingRad = Math.toRadians(self.getDirectionDeg());
        double selfFutureX = self.getX() + Math.cos(selfHeadingRad) * self.getSpeed() * COLLISION_LOOKAHEAD_SECONDS;
        double selfFutureY = self.getY() + Math.sin(selfHeadingRad) * self.getSpeed() * COLLISION_LOOKAHEAD_SECONDS;

        return distance(selfFutureX, selfFutureY, otherFutureX, otherFutureY) <= collisionDistance;
    }

    private void applyEmergencyManeuver(VehicleState state, VehicleState threat) {
        double dx = state.getX() - threat.getX();
        double dy = state.getY() - threat.getY();
        double separation = distance(state.getX(), state.getY(), threat.getX(), threat.getY());
        double minimumSeparation = state.getRadius() + threat.getRadius() + 2.0;

        if (separation < minimumSeparation) {
            state.setSpeed(0.0);
            state.setStatus(VehicleStatus.STOPPED);
            state.setCurrentAction(AvoidanceAction.EMERGENCY_STOP);
            setTargetDirection(Math.toDegrees(Math.atan2(dy, dx)));
            return;
        }

        state.setStatus(VehicleStatus.ACTIVE);
        state.setCurrentAction(AvoidanceAction.SLOW_DOWN);
        state.setSpeed(Math.max(20.0, state.getSpeed() * 0.7));
        setTargetDirection(Math.toDegrees(Math.atan2(dy, dx)));
    }

    private void aiTick() {
        if (manualOverrideActive.get()) {
            if (System.currentTimeMillis() < manualOverrideUntilMillis.get()) {
                return;
            }
            manualOverrideActive.set(false);
        }
        VehicleState current = selfState.get();
        List<VehicleState> context = new ArrayList<>(nearbyVehicles.values());
        AvoidanceDecisionEngine.DecisionResult result = decisionEngine.choose(current.copy(), context);

        current.setCurrentRiskScore(result.getRiskScore());
        current.setRiskLevel(result.getRiskLevel());
        current.setCurrentAction(result.getAction());

        switch (result.getAction()) {
            case TURN_LEFT:
                setTargetDirection(getTargetDirection() - 20.0);
                break;
            case TURN_RIGHT:
                setTargetDirection(getTargetDirection() + 20.0);
                break;
            case SLOW_DOWN:
                current.setSpeed(Math.max(15.0, current.getSpeed() * 0.85));
                break;
            case EMERGENCY_STOP:
                current.setSpeed(0.0);
                current.setStatus(VehicleStatus.STOPPED);
                break;
            case KEEP_COURSE:
                if (current.getStatus() == VehicleStatus.STOPPED) {
                    current.setSpeed(config.getInitialSpeed());
                }
                current.setStatus(VehicleStatus.ACTIVE);
                break;
            default:
                break;
        }
    }

    private void publishState() {
        outboundQueue.offer(new WireMessage(MessageType.STATE_UPDATE, selfState.get().copy()));
    }

    private void bounceIfNeeded(VehicleState state) {
        double radius = state.getRadius();
        boolean bounced = false;
        if (state.getX() <= radius) {
            state.setX(radius);
            state.setDirectionDeg(normalizeDirection(180.0 - state.getDirectionDeg()));
            bounced = true;
        } else if (state.getX() >= config.getWorldWidth() - radius) {
            state.setX(config.getWorldWidth() - radius);
            state.setDirectionDeg(normalizeDirection(180.0 - state.getDirectionDeg()));
            bounced = true;
        }

        if (state.getY() <= radius) {
            state.setY(radius);
            state.setDirectionDeg(normalizeDirection(360.0 - state.getDirectionDeg()));
            bounced = true;
        } else if (state.getY() >= config.getWorldHeight() - radius) {
            state.setY(config.getWorldHeight() - radius);
            state.setDirectionDeg(normalizeDirection(360.0 - state.getDirectionDeg()));
            bounced = true;
        }

        if (bounced && state.getSpeed() <= 0.0) {
            state.setSpeed(config.getInitialSpeed());
            state.setStatus(VehicleStatus.ACTIVE);
        }

        if (bounced) {
            setTargetDirection(state.getDirectionDeg());
        }
    }

    private void setTargetDirection(double direction) {
        long scaled = Math.round(normalizeDirection(direction) * 100.0);
        targetDirectionDegTimes100.set(scaled);
    }

    private double getTargetDirection() {
        return targetDirectionDegTimes100.get() / 100.0;
    }

    private void rotateTowardsTarget(VehicleState state, double maxTurnDegPerTick) {
        double current = normalizeDirection(state.getDirectionDeg());
        double target = getTargetDirection();
        double delta = shortestSignedDeltaDeg(current, target);
        double boundedDelta = Math.max(-maxTurnDegPerTick, Math.min(maxTurnDegPerTick, delta));
        state.setDirectionDeg(normalizeDirection(current + boundedDelta));
    }

    private double shortestSignedDeltaDeg(double fromDeg, double toDeg) {
        double delta = (toDeg - fromDeg + 540.0) % 360.0 - 180.0;
        if (delta == -180.0) {
            return 180.0;
        }
        return delta;
    }

    private double normalizeDirection(double direction) {
        double normalized = direction % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }
}
