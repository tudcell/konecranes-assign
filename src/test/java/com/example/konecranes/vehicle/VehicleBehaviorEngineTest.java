package com.example.konecranes.vehicle;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.messaging.EnvironmentUpdate;
import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VehicleBehaviorEngineTest {

    @Test
    void currentStateCopyReturnsDetachedInitialState() {
        VehicleBehaviorEngine vehicleBehaviorEngine = new VehicleBehaviorEngine(testConfig());

        VehicleState firstCopy = vehicleBehaviorEngine.currentStateCopy();
        VehicleState secondCopy = vehicleBehaviorEngine.currentStateCopy();

        assertEquals("VH-TEST", firstCopy.getId());
        assertEquals(100.0, firstCopy.getX());
        assertEquals(100.0, firstCopy.getY());
        assertEquals(0.0, firstCopy.getDirectionDeg());
        assertEquals(60.0, firstCopy.getSpeed());
        assertEquals(16.0, firstCopy.getRadius());
        assertEquals(VehicleStatus.ACTIVE, firstCopy.getStatus());

        firstCopy.setX(999.0);

        assertEquals(100.0, secondCopy.getX());
        assertNotSame(firstCopy, secondCopy);
    }

    @Test
    void movementTickAdvancesPositionWhenNoThreatExists() {
        VehicleBehaviorEngine vehicleBehaviorEngine = new VehicleBehaviorEngine(testConfig());

        EnvironmentUpdate environmentUpdate = new EnvironmentUpdate();
        environmentUpdate.setNearbyVehicles(List.of());
        vehicleBehaviorEngine.onEnvironmentUpdate(environmentUpdate);

        vehicleBehaviorEngine.movementTick();

        VehicleState state = vehicleBehaviorEngine.currentStateCopy();

        assertEquals(106.0, state.getX(), 0.0001);
        assertEquals(100.0, state.getY(), 0.0001);
        assertEquals(0.0, state.getDirectionDeg(), 0.0001);
        assertEquals(VehicleStatus.ACTIVE, state.getStatus());
    }

    @Test
    void movementTickAppliesEmergencyManeuverWhenThreatIsImmediate() {
        VehicleBehaviorEngine vehicleBehaviorEngine = new VehicleBehaviorEngine(testConfig());

        VehicleState nearbyVehicle = vehicle("VH-OTHER", 120.0, 100.0, 180.0, 20.0, 16.0);

        EnvironmentUpdate environmentUpdate = new EnvironmentUpdate();
        environmentUpdate.setNearbyVehicles(List.of(nearbyVehicle));
        vehicleBehaviorEngine.onEnvironmentUpdate(environmentUpdate);

        vehicleBehaviorEngine.movementTick();

        VehicleState state = vehicleBehaviorEngine.currentStateCopy();

        assertEquals(VehicleStatus.STOPPED, state.getStatus());
        assertEquals(AvoidanceAction.EMERGENCY_STOP, state.getCurrentAction());
        assertEquals(15.0, state.getSpeed(), 0.0001); // 60 * 0.25
    }

    @Test
    void onControlCommandAppliesManualSpeedAndDirectionTarget() {
        VehicleBehaviorEngine vehicleBehaviorEngine = new VehicleBehaviorEngine(testConfig());

        ControlCommand controlCommand = new ControlCommand();
        controlCommand.setVehicleId("VH-TEST");
        controlCommand.setOverrideDirectionDeg(90.0);
        controlCommand.setOverrideSpeed(30.0);
        controlCommand.setManualOverride(true);

        vehicleBehaviorEngine.onControlCommand(controlCommand);
        vehicleBehaviorEngine.movementTick();

        VehicleState state = vehicleBehaviorEngine.currentStateCopy();

        assertEquals(30.0, state.getSpeed(), 0.0001);
        assertEquals(AvoidanceAction.USER_OVERRIDE, state.getCurrentAction());
        assertEquals(8.0, state.getDirectionDeg(), 0.0001); // limited by maxTurnDegPerTick
    }

    @Test
    void movementTickTriggersStuckRecoveryWhenVehicleDoesNotMoveForTooLong() throws InterruptedException {
        VehicleBehaviorEngine vehicleBehaviorEngine = new VehicleBehaviorEngine(stuckRecoveryConfig());

        ControlCommand stopCommand = new ControlCommand();
        stopCommand.setVehicleId("VH-TEST");
        stopCommand.setOverrideSpeed(0.0);
        stopCommand.setManualOverride(false);
        vehicleBehaviorEngine.onControlCommand(stopCommand);

        Thread.sleep(30L); // longer than stuckTimeMillis in test config

        vehicleBehaviorEngine.movementTick();

        VehicleState state = vehicleBehaviorEngine.currentStateCopy();

        assertEquals(AvoidanceAction.EMERGENCY_STOP, state.getCurrentAction());
        assertEquals(180.0, state.getDirectionDeg(), 0.0001);
        assertEquals(15.0, state.getSpeed(), 0.0001); // 10 * 1.5
    }

    private VehicleProcessConfig testConfig() {
        return VehicleProcessConfig.fromArgs(new String[]{
                "--vehicleId=VH-TEST",
                "--gatewayHost=127.0.0.1",
                "--gatewayPort=9090",
                "--worldWidth=1000",
                "--worldHeight=700",
                "--initialX=100",
                "--initialY=100",
                "--initialDirectionDeg=0",
                "--initialSpeed=60",
                "--tickMillis=100",
                "--maxTurnDegPerTick=8.0",
                "--manualOverrideHoldMillis=2000",
                "--aiTurnDeltaDeg=8.0",
                "--aiSlowDownFactor=0.90",
                "--aiRecoveryFactor=1.08",
                "--aiPredictionSteps=20",
                "--aiPredictionStepSeconds=0.1",
                "--aiKeepCourseRiskThreshold=0.12",
                "--safetyEmergencyMargin=3.0",
                "--safetyEmergencyLookaheadSeconds=0.08",
                "--safetyHardStopFactor=0.25",
                "--safetySoftBrakeFactor=0.80",
                "--safetySoftBrakeMinimumSpeed=20.0",
                "--stuckDistanceThreshold=1.0",
                "--stuckTimeMillis=2000",
                "--stuckEscapeSpeedFactor=1.5"
        });
    }

    private VehicleProcessConfig stuckRecoveryConfig() {
        return VehicleProcessConfig.fromArgs(new String[]{
                "--vehicleId=VH-TEST",
                "--gatewayHost=127.0.0.1",
                "--gatewayPort=9090",
                "--worldWidth=1000",
                "--worldHeight=700",
                "--initialX=100",
                "--initialY=100",
                "--initialDirectionDeg=0",
                "--initialSpeed=10",
                "--tickMillis=100",
                "--maxTurnDegPerTick=8.0",
                "--manualOverrideHoldMillis=2000",
                "--aiTurnDeltaDeg=8.0",
                "--aiSlowDownFactor=0.90",
                "--aiRecoveryFactor=1.08",
                "--aiPredictionSteps=20",
                "--aiPredictionStepSeconds=0.1",
                "--aiKeepCourseRiskThreshold=0.12",
                "--safetyEmergencyMargin=3.0",
                "--safetyEmergencyLookaheadSeconds=0.08",
                "--safetyHardStopFactor=0.25",
                "--safetySoftBrakeFactor=0.80",
                "--safetySoftBrakeMinimumSpeed=20.0",
                "--stuckDistanceThreshold=1.0",
                "--stuckTimeMillis=10",
                "--stuckEscapeSpeedFactor=1.5"
        });
    }

    private VehicleState vehicle(String id,
                                 double x,
                                 double y,
                                 double directionDeg,
                                 double speed,
                                 double radius) {
        VehicleState vehicleState = new VehicleState();
        vehicleState.setId(id);
        vehicleState.setX(x);
        vehicleState.setY(y);
        vehicleState.setDirectionDeg(directionDeg);
        vehicleState.setSpeed(speed);
        vehicleState.setRadius(radius);
        vehicleState.setStatus(VehicleStatus.ACTIVE);
        vehicleState.setCurrentAction(AvoidanceAction.KEEP_COURSE);
        return vehicleState;
    }
}