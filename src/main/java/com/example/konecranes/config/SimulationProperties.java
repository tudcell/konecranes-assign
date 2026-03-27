package com.example.konecranes.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds coordinator and vehicle runtime settings from the {@code simulation.*} namespace.
 */
@Getter
@Component
@ConfigurationProperties(prefix = "simulation")
public class SimulationProperties {

    private final World world = new World();
    private final Gateway gateway = new Gateway();
    private final Scheduler scheduler = new Scheduler();
    private final Vehicle vehicle = new Vehicle();

    /**
     * World size configuration.
     */
    @Getter
    @Setter
    public static class World {
        private double width;
        private double height;

    }

    /**
     * TCP gateway host/port configuration.
     */
    @Getter
    @Setter
    public static class Gateway {
        private String host;
        private int port;

    }

    /**
     * Scheduler cadence configuration.
     */
    @Getter
    @Setter
    public static class Scheduler {
        private long fixedDelayMillis = 150L;

    }

    /**
     * Vehicle process defaults and reconnect/spawn settings.
     */
    @Getter
    public static class Vehicle {
        @Setter
        private String jarPath;
        @Setter
        private double defaultSpeed;
        @Setter
        private long tickMillis;
        @Setter
        private double spawnMinDistance = 100.0;
        @Setter
        private int spawnMaxAttempts = 50;
        @Setter
        private int reconnectMaxAttempts = 8;
        @Setter
        private long reconnectInitialBackoffMillis = 500L;
        @Setter
        private long reconnectMaxBackoffMillis = 5000L;
        private final Tuning tuning = new Tuning();

    }

    /**
     * Fine-grained AI, motion, and safety tuning values.
     */
    @Setter
    @Getter
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

    }
}
