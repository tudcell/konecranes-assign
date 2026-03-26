package com.example.konecranes.application;

import com.example.konecranes.application.port.in.VehicleSpawnUseCase;
import com.example.konecranes.application.port.out.VehicleProcessHandle;
import com.example.konecranes.application.port.out.VehicleProcessLauncherPort;
import com.example.konecranes.application.port.out.VehicleStateRepository;
import com.example.konecranes.config.SimulationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Application service that spawns and owns vehicle child processes.
 */
@Service
public class VehicleSpawnerService implements VehicleSpawnUseCase {

    private static final Logger logger = LoggerFactory.getLogger(VehicleSpawnerService.class);
    private static final long PROCESS_TERMINATION_TIMEOUT_MILLIS = 3000L;

    private final SimulationProperties properties;
    private final VehicleStateRepository vehicleStateRepository;
    private final VehicleProcessLauncherPort processLauncher;
    private final Map<String, VehicleProcessHandle> spawnedProcesses = new ConcurrentHashMap<>();

    public VehicleSpawnerService(SimulationProperties properties,
                                 VehicleStateRepository vehicleStateRepository,
                                 VehicleProcessLauncherPort processLauncher) {
        this.properties = properties;
        this.vehicleStateRepository = vehicleStateRepository;
        this.processLauncher = processLauncher;
    }

    /**
     * Spawns the requested number of vehicle processes.
     *
     * @param count number of vehicles to create
     * @return list of created vehicle ids
     * @throws IOException when jar is missing or process launch fails
     */
    @Override
    public List<String> spawn(int count) throws IOException {
        Path jarPath = Path.of(properties.getVehicle().getJarPath()).toAbsolutePath();
        if (!Files.exists(jarPath)) {
            throw new IOException("Built jar not found at " + jarPath + ". Run 'mvn clean package' first.");
        }

        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String vehicleId = "VH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Find a safe spawn position away from existing vehicles.
            SpawnPosition position = findSafeSpawnPosition();

            List<String> command = new ArrayList<>();
            command.add(resolveJavaExecutable());
            command.add("-jar");
            command.add(jarPath.toString());
            command.add("--mode=vehicle");
            command.add("--vehicleId=" + vehicleId);
            command.add("--gatewayHost=" + properties.getGateway().getHost());
            command.add("--gatewayPort=" + properties.getGateway().getPort());
            command.add("--worldWidth=" + properties.getWorld().getWidth());
            command.add("--worldHeight=" + properties.getWorld().getHeight());
            command.add("--initialX=" + position.x);
            command.add("--initialY=" + position.y);
            command.add("--initialDirectionDeg=" + random(0.0, 359.0));
            command.add("--initialSpeed=" + properties.getVehicle().getDefaultSpeed());
            command.add("--tickMillis=" + properties.getVehicle().getTickMillis());
            command.add("--maxTurnDegPerTick=" + properties.getVehicle().getTuning().getMaxTurnDegPerTick());
            command.add("--manualOverrideHoldMillis=" + properties.getVehicle().getTuning().getManualOverrideHoldMillis());
            command.add("--aiTurnDeltaDeg=" + properties.getVehicle().getTuning().getAiTurnDeltaDeg());
            command.add("--aiSlowDownFactor=" + properties.getVehicle().getTuning().getAiSlowDownFactor());
            command.add("--aiRecoveryFactor=" + properties.getVehicle().getTuning().getAiRecoveryFactor());
            command.add("--aiPredictionSteps=" + properties.getVehicle().getTuning().getAiPredictionSteps());
            command.add("--aiPredictionStepSeconds=" + properties.getVehicle().getTuning().getAiPredictionStepSeconds());
            command.add("--aiKeepCourseRiskThreshold=" + properties.getVehicle().getTuning().getAiKeepCourseRiskThreshold());
            command.add("--safetyEmergencyMargin=" + properties.getVehicle().getTuning().getSafetyEmergencyMargin());
            command.add("--safetyEmergencyLookaheadSeconds=" + properties.getVehicle().getTuning().getSafetyEmergencyLookaheadSeconds());
            command.add("--safetyHardStopFactor=" + properties.getVehicle().getTuning().getSafetyHardStopFactor());
            command.add("--safetySoftBrakeFactor=" + properties.getVehicle().getTuning().getSafetySoftBrakeFactor());
            command.add("--safetySoftBrakeMinimumSpeed=" + properties.getVehicle().getTuning().getSafetySoftBrakeMinimumSpeed());
            command.add("--stuckDistanceThreshold=" + properties.getVehicle().getTuning().getStuckDistanceThreshold());
            command.add("--stuckTimeMillis=" + properties.getVehicle().getTuning().getStuckTimeMillis());
            command.add("--stuckEscapeSpeedFactor=" + properties.getVehicle().getTuning().getStuckEscapeSpeedFactor());
            command.add("--reconnectMaxAttempts=" + properties.getVehicle().getReconnectMaxAttempts());
            command.add("--reconnectInitialBackoffMillis=" + properties.getVehicle().getReconnectInitialBackoffMillis());
            command.add("--reconnectMaxBackoffMillis=" + properties.getVehicle().getReconnectMaxBackoffMillis());

            VehicleProcessHandle processHandle = processLauncher.launch(command);
            spawnedProcesses.put(vehicleId, processHandle);
            ids.add(vehicleId);
        }
        return ids;
    }

    /**
     * Stops all spawned child processes during coordinator shutdown.
     */
    @PreDestroy
    public void stopSpawnedVehicles() {
        for (Map.Entry<String, VehicleProcessHandle> entry : spawnedProcesses.entrySet()) {
            String vehicleId = entry.getKey();
            VehicleProcessHandle processHandle = entry.getValue();
            if (!processHandle.isAlive()) {
                continue;
            }
            processHandle.destroy();
            try {
                if (!processHandle.waitFor(PROCESS_TERMINATION_TIMEOUT_MILLIS)) {
                    logger.warn("Vehicle process {} did not stop in {}ms; forcing termination", vehicleId, PROCESS_TERMINATION_TIMEOUT_MILLIS);
                    processHandle.destroyForcibly();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while stopping vehicle process {}", vehicleId, ex);
            }
        }
        spawnedProcesses.clear();
    }

    /**
     * Finds a spawn location that is not too close to existing vehicles.
     *
     * @return selected spawn position
     */
    private SpawnPosition findSafeSpawnPosition() {
        List<SpawnPosition> existingPositions = vehicleStateRepository.findAll().stream()
                .map(v -> new SpawnPosition(v.getX(), v.getY()))
                .collect(Collectors.toList());

        int attempts = 0;
        while (attempts < properties.getVehicle().getSpawnMaxAttempts()) {
            double x = random(50.0, properties.getWorld().getWidth() - 50.0);
            double y = random(50.0, properties.getWorld().getHeight() - 50.0);

            boolean tooClose = existingPositions.stream()
                    .anyMatch(pos -> distance(x, y, pos.x, pos.y) < properties.getVehicle().getSpawnMinDistance());

            if (!tooClose) {
                return new SpawnPosition(x, y);
            }
            attempts++;
        }

        // Fallback: return random position if no safe spot found after max attempts.
        return new SpawnPosition(random(50.0, properties.getWorld().getWidth() - 50.0),
                random(50.0, properties.getWorld().getHeight() - 50.0));
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    private String resolveJavaExecutable() {
        String javaHome = System.getProperty("java.home");
        return Path.of(javaHome, "bin", "java").toString();
    }

    private double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private static class SpawnPosition {
        final double x;
        final double y;

        SpawnPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}

