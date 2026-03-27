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
 * Application service responsible for spawning and tracking vehicle processes.
 *
 * Creates child JVM processes for vehicles and keeps handles
 * so they can be stopped during coordinator shutdown.
 */
@Service
public class VehicleSpawnerService implements VehicleSpawnUseCase {

    private static final Logger logger = LoggerFactory.getLogger(VehicleSpawnerService.class);
    private static final long PROCESS_TERMINATION_TIMEOUT_MILLIS = 3000L;

    private final SimulationProperties simulationProperties;
    private final VehicleStateRepository vehicleStateRepository;
    private final VehicleProcessLauncherPort vehicleProcessLauncherPort;
    private final Map<String, VehicleProcessHandle> spawnedVehicleProcesses = new ConcurrentHashMap<>();

    public VehicleSpawnerService(SimulationProperties simulationProperties,
                                 VehicleStateRepository vehicleStateRepository,
                                 VehicleProcessLauncherPort vehicleProcessLauncherPort) {
        this.simulationProperties = simulationProperties;
        this.vehicleStateRepository = vehicleStateRepository;
        this.vehicleProcessLauncherPort = vehicleProcessLauncherPort;
    }

    /**
     * Spawns the requested number of vehicle processes.
     *
     * For each vehicle, this method:
     * - generates a new vehicle id
     * - selects a spawn location
     * - builds the child JVM command
     * - launches the child process
     * - stores its process handle
     *
     * @param count number of vehicles to create
     * @return created vehicle ids
     * @throws IOException when the packaged jar is missing or process launch fails
     */
    @Override
    public List<String> spawn(int count) throws IOException {
        Path jarPath = Path.of(simulationProperties.getVehicle().getJarPath()).toAbsolutePath();
        if (!Files.exists(jarPath)) {
            throw new IOException("Built jar not found at " + jarPath + ". Run 'mvn clean package' first.");
        }

        List<String> vehicleIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String vehicleId = "VH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            SpawnPosition spawnPosition = findSafeSpawnPosition();

            List<String> command = new ArrayList<>();
            command.add(resolveJavaExecutable());
            command.add("-jar");
            command.add(jarPath.toString());
            command.add("--mode=vehicle");
            command.add("--vehicleId=" + vehicleId);
            command.add("--gatewayHost=" + simulationProperties.getGateway().getHost());
            command.add("--gatewayPort=" + simulationProperties.getGateway().getPort());
            command.add("--worldWidth=" + simulationProperties.getWorld().getWidth());
            command.add("--worldHeight=" + simulationProperties.getWorld().getHeight());
            command.add("--initialX=" + spawnPosition.x);
            command.add("--initialY=" + spawnPosition.y);
            command.add("--initialDirectionDeg=" + random(0.0, 359.0));
            command.add("--initialSpeed=" + simulationProperties.getVehicle().getDefaultSpeed());
            command.add("--tickMillis=" + simulationProperties.getVehicle().getTickMillis());
            command.add("--maxTurnDegPerTick=" + simulationProperties.getVehicle().getTuning().getMaxTurnDegPerTick());
            command.add("--manualOverrideHoldMillis=" + simulationProperties.getVehicle().getTuning().getManualOverrideHoldMillis());
            command.add("--aiTurnDeltaDeg=" + simulationProperties.getVehicle().getTuning().getAiTurnDeltaDeg());
            command.add("--aiSlowDownFactor=" + simulationProperties.getVehicle().getTuning().getAiSlowDownFactor());
            command.add("--aiRecoveryFactor=" + simulationProperties.getVehicle().getTuning().getAiRecoveryFactor());
            command.add("--aiPredictionSteps=" + simulationProperties.getVehicle().getTuning().getAiPredictionSteps());
            command.add("--aiPredictionStepSeconds=" + simulationProperties.getVehicle().getTuning().getAiPredictionStepSeconds());
            command.add("--aiKeepCourseRiskThreshold=" + simulationProperties.getVehicle().getTuning().getAiKeepCourseRiskThreshold());
            command.add("--safetyEmergencyMargin=" + simulationProperties.getVehicle().getTuning().getSafetyEmergencyMargin());
            command.add("--safetyEmergencyLookaheadSeconds=" + simulationProperties.getVehicle().getTuning().getSafetyEmergencyLookaheadSeconds());
            command.add("--safetyHardStopFactor=" + simulationProperties.getVehicle().getTuning().getSafetyHardStopFactor());
            command.add("--safetySoftBrakeFactor=" + simulationProperties.getVehicle().getTuning().getSafetySoftBrakeFactor());
            command.add("--safetySoftBrakeMinimumSpeed=" + simulationProperties.getVehicle().getTuning().getSafetySoftBrakeMinimumSpeed());
            command.add("--stuckDistanceThreshold=" + simulationProperties.getVehicle().getTuning().getStuckDistanceThreshold());
            command.add("--stuckTimeMillis=" + simulationProperties.getVehicle().getTuning().getStuckTimeMillis());
            command.add("--stuckEscapeSpeedFactor=" + simulationProperties.getVehicle().getTuning().getStuckEscapeSpeedFactor());
            command.add("--reconnectMaxAttempts=" + simulationProperties.getVehicle().getReconnectMaxAttempts());
            command.add("--reconnectInitialBackoffMillis=" + simulationProperties.getVehicle().getReconnectInitialBackoffMillis());
            command.add("--reconnectMaxBackoffMillis=" + simulationProperties.getVehicle().getReconnectMaxBackoffMillis());

            VehicleProcessHandle vehicleProcessHandle = vehicleProcessLauncherPort.launch(command);
            spawnedVehicleProcesses.put(vehicleId, vehicleProcessHandle);
            vehicleIds.add(vehicleId);
        }

        return vehicleIds;
    }

    /**
     * Stops all spawned vehicle processes during coordinator shutdown.
     *
     * First requests graceful shutdown, then forces termination
     * if a process does not stop within the configured timeout.
     */
    @PreDestroy
    public void stopSpawnedVehicles() {
        for (Map.Entry<String, VehicleProcessHandle> entry : spawnedVehicleProcesses.entrySet()) {
            String vehicleId = entry.getKey();
            VehicleProcessHandle vehicleProcessHandle = entry.getValue();

            if (!vehicleProcessHandle.isAlive()) {
                continue;
            }

            vehicleProcessHandle.destroy();

            try {
                if (!vehicleProcessHandle.waitFor(PROCESS_TERMINATION_TIMEOUT_MILLIS)) {
                    logger.warn(
                            "Vehicle process {} did not stop in {}ms; forcing termination",
                            vehicleId,
                            PROCESS_TERMINATION_TIMEOUT_MILLIS
                    );
                    vehicleProcessHandle.destroyForcibly();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while stopping vehicle process {}", vehicleId, ex);
            }
        }

        spawnedVehicleProcesses.clear();
    }

    /**
     * Finds a spawn location that is not too close to existing vehicles.
     *
     * The search tries a bounded number of random positions.
     * If no safe position is found, a fallback random position is returned.
     *
     * @return selected spawn position
     */
    private SpawnPosition findSafeSpawnPosition() {
        List<SpawnPosition> existingPositions = vehicleStateRepository.findAll().stream()
                .map(vehicle -> new SpawnPosition(vehicle.getX(), vehicle.getY()))
                .collect(Collectors.toList());

        int attempts = 0;
        while (attempts < simulationProperties.getVehicle().getSpawnMaxAttempts()) {
            double x = random(50.0, simulationProperties.getWorld().getWidth() - 50.0);
            double y = random(50.0, simulationProperties.getWorld().getHeight() - 50.0);

            boolean tooClose = existingPositions.stream()
                    .anyMatch(position ->
                            distance(x, y, position.x, position.y)
                                    < simulationProperties.getVehicle().getSpawnMinDistance());

            if (!tooClose) {
                return new SpawnPosition(x, y);
            }

            attempts++;
        }

        return new SpawnPosition(
                random(50.0, simulationProperties.getWorld().getWidth() - 50.0),
                random(50.0, simulationProperties.getWorld().getHeight() - 50.0)
        );
    }

    /**
     * Computes Euclidean distance between two points.
     *
     * @param x1 first point x
     * @param y1 first point y
     * @param x2 second point x
     * @param y2 second point y
     * @return distance
     */
    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    /**
     * Resolves the current JVM executable path.
     *
     * @return absolute path to the Java executable
     */
    private String resolveJavaExecutable() {
        String javaHome = System.getProperty("java.home");
        return Path.of(javaHome, "bin", "java").toString();
    }

    /**
     * Returns a random double in the given range.
     *
     * @param min lower bound
     * @param max upper bound
     * @return random value in [min, max)
     */
    private double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Small value object representing one spawn position.
     */
    private static class SpawnPosition {
        final double x;
        final double y;

        SpawnPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}