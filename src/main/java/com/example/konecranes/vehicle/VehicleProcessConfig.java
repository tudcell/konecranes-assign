package com.example.konecranes.vehicle;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for one spawned vehicle process.
 *
 * Values are parsed once from command-line arguments
 * provided by the coordinator at process startup.
 */
@Getter
public class VehicleProcessConfig {

    private String vehicleId;
    private String gatewayHost;
    private int gatewayPort;
    private double worldWidth;
    private double worldHeight;
    private double initialX;
    private double initialY;
    private double initialDirectionDeg;
    private double initialSpeed;
    private long tickMillis;
    private final double radius = 16.0;
    private double maxTurnDegPerTick;
    private long manualOverrideHoldMillis;
    private double aiTurnDeltaDeg;
    private double aiSlowDownFactor;
    private double aiRecoveryFactor;
    private int aiPredictionSteps;
    private double aiPredictionStepSeconds;
    private double aiKeepCourseRiskThreshold;
    private double safetyEmergencyMargin;
    private double safetyEmergencyLookaheadSeconds;
    private double safetyHardStopFactor;
    private double safetySoftBrakeFactor;
    private double safetySoftBrakeMinimumSpeed;
    private double stuckDistanceThreshold;
    private long stuckTimeMillis;
    private double stuckEscapeSpeedFactor;
    private int reconnectMaxAttempts;
    private long reconnectInitialBackoffMillis;
    private long reconnectMaxBackoffMillis;

    /**
     * Parses command-line arguments into a vehicle process configuration.
     *
     * Expected format:
     * --key=value
     *
     * Required argument:
     * - vehicleId
     *
     * Other values fall back to defaults when not provided.
     *
     * @param args process arguments
     * @return parsed vehicle process configuration
     */
    public static VehicleProcessConfig fromArgs(String[] args) {
        Map<String, String> values = new HashMap<>();

        for (String arg : args) {
            if (arg.startsWith("--") && arg.contains("=")) {
                String[] parts = arg.substring(2).split("=", 2);
                values.put(parts[0], parts[1]);
            }
        }

        VehicleProcessConfig config = new VehicleProcessConfig();

        String vehicleId = values.get("vehicleId");
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("vehicleId is required");
        }

        config.vehicleId = vehicleId;
        config.gatewayHost = values.getOrDefault("gatewayHost", "127.0.0.1");
        config.gatewayPort = Integer.parseInt(values.getOrDefault("gatewayPort", "9090"));
        config.worldWidth = Double.parseDouble(values.getOrDefault("worldWidth", "1000"));
        config.worldHeight = Double.parseDouble(values.getOrDefault("worldHeight", "700"));
        config.initialX = Double.parseDouble(values.getOrDefault("initialX", "100"));
        config.initialY = Double.parseDouble(values.getOrDefault("initialY", "100"));
        config.initialDirectionDeg = Double.parseDouble(values.getOrDefault("initialDirectionDeg", "0"));
        config.initialSpeed = Double.parseDouble(values.getOrDefault("initialSpeed", "50"));
        config.tickMillis = Long.parseLong(values.getOrDefault("tickMillis", "100"));
        config.maxTurnDegPerTick = Double.parseDouble(values.getOrDefault("maxTurnDegPerTick", "8.0"));
        config.manualOverrideHoldMillis = Long.parseLong(values.getOrDefault("manualOverrideHoldMillis", "2000"));
        config.aiTurnDeltaDeg = Double.parseDouble(values.getOrDefault("aiTurnDeltaDeg", "8.0"));
        config.aiSlowDownFactor = Double.parseDouble(values.getOrDefault("aiSlowDownFactor", "0.90"));
        config.aiRecoveryFactor = Double.parseDouble(values.getOrDefault("aiRecoveryFactor", "1.08"));
        config.aiPredictionSteps = Integer.parseInt(values.getOrDefault("aiPredictionSteps", "20"));
        config.aiPredictionStepSeconds = Double.parseDouble(values.getOrDefault("aiPredictionStepSeconds", "0.1"));
        config.aiKeepCourseRiskThreshold = Double.parseDouble(values.getOrDefault("aiKeepCourseRiskThreshold", "0.12"));
        config.safetyEmergencyMargin = Double.parseDouble(values.getOrDefault("safetyEmergencyMargin", "3.0"));
        config.safetyEmergencyLookaheadSeconds = Double.parseDouble(values.getOrDefault("safetyEmergencyLookaheadSeconds", "0.08"));
        config.safetyHardStopFactor = Double.parseDouble(values.getOrDefault("safetyHardStopFactor", "0.25"));
        config.safetySoftBrakeFactor = Double.parseDouble(values.getOrDefault("safetySoftBrakeFactor", "0.80"));
        config.safetySoftBrakeMinimumSpeed = Double.parseDouble(values.getOrDefault("safetySoftBrakeMinimumSpeed", "20.0"));
        config.stuckDistanceThreshold = Double.parseDouble(values.getOrDefault("stuckDistanceThreshold", "1.0"));
        config.stuckTimeMillis = Long.parseLong(values.getOrDefault("stuckTimeMillis", "2000"));
        config.stuckEscapeSpeedFactor = Double.parseDouble(values.getOrDefault("stuckEscapeSpeedFactor", "1.5"));
        config.reconnectMaxAttempts = Integer.parseInt(values.getOrDefault("reconnectMaxAttempts", "8"));
        config.reconnectInitialBackoffMillis = Long.parseLong(values.getOrDefault("reconnectInitialBackoffMillis", "500"));
        config.reconnectMaxBackoffMillis = Long.parseLong(values.getOrDefault("reconnectMaxBackoffMillis", "5000"));

        return config;
    }
}