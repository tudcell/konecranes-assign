package com.example.konecranes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "simulation")
public class SimulationProperties {

    private final World world = new World();
    private final Gateway gateway = new Gateway();
    private final Scheduler scheduler = new Scheduler();
    private final Vehicle vehicle = new Vehicle();

    public World getWorld() {
        return world;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public static class World {
        private double width;
        private double height;

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }
    }

    public static class Gateway {
        private String host;
        private int port;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class Scheduler {
        private long fixedDelayMillis = 150L;

        public long getFixedDelayMillis() {
            return fixedDelayMillis;
        }

        public void setFixedDelayMillis(long fixedDelayMillis) {
            this.fixedDelayMillis = fixedDelayMillis;
        }
    }

    public static class Vehicle {
        private String jarPath;
        private double defaultSpeed;
        private long tickMillis;
        private double spawnMinDistance = 100.0;
        private int spawnMaxAttempts = 50;
        private final Tuning tuning = new Tuning();

        public String getJarPath() {
            return jarPath;
        }

        public void setJarPath(String jarPath) {
            this.jarPath = jarPath;
        }

        public double getDefaultSpeed() {
            return defaultSpeed;
        }

        public void setDefaultSpeed(double defaultSpeed) {
            this.defaultSpeed = defaultSpeed;
        }

        public long getTickMillis() {
            return tickMillis;
        }

        public void setTickMillis(long tickMillis) {
            this.tickMillis = tickMillis;
        }

        public double getSpawnMinDistance() {
            return spawnMinDistance;
        }

        public void setSpawnMinDistance(double spawnMinDistance) {
            this.spawnMinDistance = spawnMinDistance;
        }

        public int getSpawnMaxAttempts() {
            return spawnMaxAttempts;
        }

        public void setSpawnMaxAttempts(int spawnMaxAttempts) {
            this.spawnMaxAttempts = spawnMaxAttempts;
        }

        public Tuning getTuning() {
            return tuning;
        }
    }

    public static class Tuning {
        private double maxTurnDegPerTick = 8.0;
        private long manualOverrideHoldMillis = 2000L;
        private double aiTurnDeltaDeg = 8.0;
        private double aiSlowDownFactor = 0.90;
        private double aiRecoveryFactor = 1.08;
        private int aiPredictionSteps = 20;
        private double aiPredictionStepSeconds = 0.10;
        private double aiKeepCourseRiskThreshold = 0.12;
        private double safetyEmergencyMargin = 3.0;
        private double safetyEmergencyLookaheadSeconds = 0.08;
        private double safetyHardStopFactor = 0.25;
        private double safetySoftBrakeFactor = 0.80;
        private double safetySoftBrakeMinimumSpeed = 20.0;
        private double stuckDistanceThreshold = 1.0;
        private long stuckTimeMillis = 2000L;
        private double stuckEscapeSpeedFactor = 1.5;

        public double getMaxTurnDegPerTick() {
            return maxTurnDegPerTick;
        }

        public void setMaxTurnDegPerTick(double maxTurnDegPerTick) {
            this.maxTurnDegPerTick = maxTurnDegPerTick;
        }

        public long getManualOverrideHoldMillis() {
            return manualOverrideHoldMillis;
        }

        public void setManualOverrideHoldMillis(long manualOverrideHoldMillis) {
            this.manualOverrideHoldMillis = manualOverrideHoldMillis;
        }

        public double getAiTurnDeltaDeg() {
            return aiTurnDeltaDeg;
        }

        public void setAiTurnDeltaDeg(double aiTurnDeltaDeg) {
            this.aiTurnDeltaDeg = aiTurnDeltaDeg;
        }

        public double getAiSlowDownFactor() {
            return aiSlowDownFactor;
        }

        public void setAiSlowDownFactor(double aiSlowDownFactor) {
            this.aiSlowDownFactor = aiSlowDownFactor;
        }

        public double getAiRecoveryFactor() {
            return aiRecoveryFactor;
        }

        public void setAiRecoveryFactor(double aiRecoveryFactor) {
            this.aiRecoveryFactor = aiRecoveryFactor;
        }

        public int getAiPredictionSteps() {
            return aiPredictionSteps;
        }

        public void setAiPredictionSteps(int aiPredictionSteps) {
            this.aiPredictionSteps = aiPredictionSteps;
        }

        public double getAiPredictionStepSeconds() {
            return aiPredictionStepSeconds;
        }

        public void setAiPredictionStepSeconds(double aiPredictionStepSeconds) {
            this.aiPredictionStepSeconds = aiPredictionStepSeconds;
        }

        public double getAiKeepCourseRiskThreshold() {
            return aiKeepCourseRiskThreshold;
        }

        public void setAiKeepCourseRiskThreshold(double aiKeepCourseRiskThreshold) {
            this.aiKeepCourseRiskThreshold = aiKeepCourseRiskThreshold;
        }

        public double getSafetyEmergencyMargin() {
            return safetyEmergencyMargin;
        }

        public void setSafetyEmergencyMargin(double safetyEmergencyMargin) {
            this.safetyEmergencyMargin = safetyEmergencyMargin;
        }

        public double getSafetyEmergencyLookaheadSeconds() {
            return safetyEmergencyLookaheadSeconds;
        }

        public void setSafetyEmergencyLookaheadSeconds(double safetyEmergencyLookaheadSeconds) {
            this.safetyEmergencyLookaheadSeconds = safetyEmergencyLookaheadSeconds;
        }

        public double getSafetyHardStopFactor() {
            return safetyHardStopFactor;
        }

        public void setSafetyHardStopFactor(double safetyHardStopFactor) {
            this.safetyHardStopFactor = safetyHardStopFactor;
        }

        public double getSafetySoftBrakeFactor() {
            return safetySoftBrakeFactor;
        }

        public void setSafetySoftBrakeFactor(double safetySoftBrakeFactor) {
            this.safetySoftBrakeFactor = safetySoftBrakeFactor;
        }

        public double getSafetySoftBrakeMinimumSpeed() {
            return safetySoftBrakeMinimumSpeed;
        }

        public void setSafetySoftBrakeMinimumSpeed(double safetySoftBrakeMinimumSpeed) {
            this.safetySoftBrakeMinimumSpeed = safetySoftBrakeMinimumSpeed;
        }

        public double getStuckDistanceThreshold() {
            return stuckDistanceThreshold;
        }

        public void setStuckDistanceThreshold(double stuckDistanceThreshold) {
            this.stuckDistanceThreshold = stuckDistanceThreshold;
        }

        public long getStuckTimeMillis() {
            return stuckTimeMillis;
        }

        public void setStuckTimeMillis(long stuckTimeMillis) {
            this.stuckTimeMillis = stuckTimeMillis;
        }

        public double getStuckEscapeSpeedFactor() {
            return stuckEscapeSpeedFactor;
        }

        public void setStuckEscapeSpeedFactor(double stuckEscapeSpeedFactor) {
            this.stuckEscapeSpeedFactor = stuckEscapeSpeedFactor;
        }
    }
}
