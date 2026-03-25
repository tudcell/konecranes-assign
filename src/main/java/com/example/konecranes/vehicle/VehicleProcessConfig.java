package com.example.konecranes.vehicle;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable-at-runtime configuration for a spawned vehicle process.
 *
 * <p>Values are parsed from command line arguments provided by the coordinator.</p>
 */
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
    private double maxTurnDegPerTick = 8.0;
    private long manualOverrideHoldMillis = 2000L;
    private double aiTurnDeltaDeg = 8.0;
    private double aiSlowDownFactor = 0.90;
    private double aiRecoveryFactor = 1.08;
    private int aiPredictionSteps = 20;
    private double aiPredictionStepSeconds = 0.1;
    private double aiKeepCourseRiskThreshold = 0.12;
    private double safetyEmergencyMargin = 3.0;
    private double safetyEmergencyLookaheadSeconds = 0.08;
    private double safetyHardStopFactor = 0.25;
    private double safetySoftBrakeFactor = 0.80;
    private double safetySoftBrakeMinimumSpeed = 20.0;
    private double stuckDistanceThreshold = 1.0;
    private long stuckTimeMillis = 2000L;
    private double stuckEscapeSpeedFactor = 1.5;
    private int reconnectMaxAttempts = 8;
    private long reconnectInitialBackoffMillis = 500L;
    private long reconnectMaxBackoffMillis = 5000L;

    /**
     * Parses command line arguments into a vehicle process configuration.
     *
     * @param args process arguments in --key=value form
     * @return parsed config object
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

    /** @return vehicle identifier */
    public String getVehicleId() { return vehicleId; }
    /** @return gateway host for TCP connection */
    public String getGatewayHost() { return gatewayHost; }
    /** @return gateway port for TCP connection */
    public int getGatewayPort() { return gatewayPort; }
    /** @return simulation world width */
    public double getWorldWidth() { return worldWidth; }
    /** @return simulation world height */
    public double getWorldHeight() { return worldHeight; }
    /** @return initial X coordinate */
    public double getInitialX() { return initialX; }
    /** @return initial Y coordinate */
    public double getInitialY() { return initialY; }
    /** @return initial heading in degrees */
    public double getInitialDirectionDeg() { return initialDirectionDeg; }
    /** @return initial speed */
    public double getInitialSpeed() { return initialSpeed; }
    /** @return movement tick interval in milliseconds */
    public long getTickMillis() { return tickMillis; }
    /** @return collision radius (fixed at 16.0 units) */
    public double getRadius() { return radius; }
    /** @return maximum turn degrees per tick */
    public double getMaxTurnDegPerTick() { return maxTurnDegPerTick; }
    /** @return manual override hold duration in milliseconds */
    public long getManualOverrideHoldMillis() { return manualOverrideHoldMillis; }
    /** @return AI turn delta in degrees for steering decisions */
    public double getAiTurnDeltaDeg() { return aiTurnDeltaDeg; }
    /** @return AI slow-down speed multiplier */
    public double getAiSlowDownFactor() { return aiSlowDownFactor; }
    /** @return AI speed recovery multiplier */
    public double getAiRecoveryFactor() { return aiRecoveryFactor; }
    /** @return AI prediction steps for lookahead */
    public int getAiPredictionSteps() { return aiPredictionSteps; }
    /** @return AI prediction time per step in seconds */
    public double getAiPredictionStepSeconds() { return aiPredictionStepSeconds; }
    /** @return AI threshold below which to keep course */
    public double getAiKeepCourseRiskThreshold() { return aiKeepCourseRiskThreshold; }
    /** @return safety emergency guard margin in units */
    public double getSafetyEmergencyMargin() { return safetyEmergencyMargin; }
    /** @return safety collision lookahead in seconds */
    public double getSafetyEmergencyLookaheadSeconds() { return safetyEmergencyLookaheadSeconds; }
    /** @return hard stop speed reduction factor */
    public double getSafetyHardStopFactor() { return safetyHardStopFactor; }
    /** @return soft brake speed reduction factor */
    public double getSafetySoftBrakeFactor() { return safetySoftBrakeFactor; }
    /** @return minimum speed for soft brake application */
    public double getSafetySoftBrakeMinimumSpeed() { return safetySoftBrakeMinimumSpeed; }
    /** @return stuck vehicle distance threshold in units */
    public double getStuckDistanceThreshold() { return stuckDistanceThreshold; }
    /** @return stuck vehicle timeout in milliseconds */
    public long getStuckTimeMillis() { return stuckTimeMillis; }
    /** @return stuck escape speed boost factor */
    public double getStuckEscapeSpeedFactor() { return stuckEscapeSpeedFactor; }
    /** @return maximum reconnect attempts before exit */
    public int getReconnectMaxAttempts() { return reconnectMaxAttempts; }
    /** @return initial reconnect backoff in milliseconds */
    public long getReconnectInitialBackoffMillis() { return reconnectInitialBackoffMillis; }
    /** @return maximum reconnect backoff in milliseconds */
    public long getReconnectMaxBackoffMillis() { return reconnectMaxBackoffMillis; }
}
