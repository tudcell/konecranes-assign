package com.example.konecranes.vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleProcessConfigTest {

    @Test
    void fromArgsThrowsWhenVehicleIdIsMissing() {
        assertThrows(IllegalArgumentException.class, () -> VehicleProcessConfig.fromArgs(new String[]{
                "--gatewayPort=9999"
        }));
    }

    @Test
    void fromArgsUsesDefaultsWhenOnlyVehicleIdProvided() {
        VehicleProcessConfig config = VehicleProcessConfig.fromArgs(new String[]{"--vehicleId=v-1"});

        assertEquals("v-1", config.getVehicleId());
        assertEquals("127.0.0.1", config.getGatewayHost());
        assertEquals(9090, config.getGatewayPort());
        assertEquals(1000.0, config.getWorldWidth());
        assertEquals(700.0, config.getWorldHeight());
        assertEquals(100.0, config.getInitialX());
        assertEquals(100.0, config.getInitialY());
        assertEquals(0.0, config.getInitialDirectionDeg());
        assertEquals(50.0, config.getInitialSpeed());
        assertEquals(100L, config.getTickMillis());
        assertEquals(8.0, config.getMaxTurnDegPerTick());
    }

    @Test
    void fromArgsParsesCustomValues() {
        VehicleProcessConfig config = VehicleProcessConfig.fromArgs(new String[]{
                "--vehicleId=v-2",
                "--gatewayHost=10.0.0.4",
                "--gatewayPort=9191",
                "--worldWidth=1300",
                "--worldHeight=900",
                "--initialX=200",
                "--initialY=300",
                "--initialDirectionDeg=270",
                "--initialSpeed=77",
                "--tickMillis=150",
                "--maxTurnDegPerTick=12",
                "--manualOverrideHoldMillis=3000",
                "--aiTurnDeltaDeg=9",
                "--aiSlowDownFactor=0.87",
                "--aiRecoveryFactor=1.05",
                "--aiPredictionSteps=25",
                "--aiPredictionStepSeconds=0.2",
                "--aiKeepCourseRiskThreshold=0.21",
                "--safetyEmergencyMargin=8",
                "--safetyEmergencyLookaheadSeconds=0.25",
                "--safetyHardStopFactor=0.22",
                "--safetySoftBrakeFactor=0.72",
                "--safetySoftBrakeMinimumSpeed=18",
                "--stuckDistanceThreshold=1.7",
                "--stuckTimeMillis=2600",
                "--stuckEscapeSpeedFactor=1.8",
                "--reconnectMaxAttempts=11",
                "--reconnectInitialBackoffMillis=400",
                "--reconnectMaxBackoffMillis=4400"
        });

        assertEquals("v-2", config.getVehicleId());
        assertEquals("10.0.0.4", config.getGatewayHost());
        assertEquals(9191, config.getGatewayPort());
        assertEquals(1300.0, config.getWorldWidth());
        assertEquals(900.0, config.getWorldHeight());
        assertEquals(200.0, config.getInitialX());
        assertEquals(300.0, config.getInitialY());
        assertEquals(270.0, config.getInitialDirectionDeg());
        assertEquals(77.0, config.getInitialSpeed());
        assertEquals(150L, config.getTickMillis());
        assertEquals(12.0, config.getMaxTurnDegPerTick());
        assertEquals(3000L, config.getManualOverrideHoldMillis());
        assertEquals(9.0, config.getAiTurnDeltaDeg());
        assertEquals(0.87, config.getAiSlowDownFactor());
        assertEquals(1.05, config.getAiRecoveryFactor());
        assertEquals(25, config.getAiPredictionSteps());
        assertEquals(0.2, config.getAiPredictionStepSeconds());
        assertEquals(0.21, config.getAiKeepCourseRiskThreshold());
        assertEquals(8.0, config.getSafetyEmergencyMargin());
        assertEquals(0.25, config.getSafetyEmergencyLookaheadSeconds());
        assertEquals(0.22, config.getSafetyHardStopFactor());
        assertEquals(0.72, config.getSafetySoftBrakeFactor());
        assertEquals(18.0, config.getSafetySoftBrakeMinimumSpeed());
        assertEquals(1.7, config.getStuckDistanceThreshold());
        assertEquals(2600L, config.getStuckTimeMillis());
        assertEquals(1.8, config.getStuckEscapeSpeedFactor());
        assertEquals(11, config.getReconnectMaxAttempts());
        assertEquals(400L, config.getReconnectInitialBackoffMillis());
        assertEquals(4400L, config.getReconnectMaxBackoffMillis());
    }
}

