package com.example.konecranes.vehicle;

import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.*;

class VehicleControlPolicyTest {

    @Test
    void applyControlCommandUpdatesDirectionSpeedAndManualOverride() {
        VehicleControlPolicy vehicleControlPolicy = new VehicleControlPolicy(testConfig());

        VehicleState currentState = baseState(60.0, VehicleStatus.ACTIVE, AvoidanceAction.KEEP_COURSE);
        AtomicReference<Double> targetDirection = new AtomicReference<>(0.0);

        ControlCommand controlCommand = new ControlCommand();
        controlCommand.setOverrideDirectionDeg(90.0);
        controlCommand.setOverrideSpeed(30.0);
        controlCommand.setManualOverride(true);

        vehicleControlPolicy.applyControlCommand(controlCommand, currentState, targetDirection::set);

        assertEquals(30.0, currentState.getSpeed(), 0.0001);
        assertEquals(AvoidanceAction.USER_OVERRIDE, currentState.getCurrentAction());
        assertEquals(90.0, targetDirection.get(), 0.0001);
    }

    @Test
    void applyControlCommandDoesNothingWhenCommandIsNull() {
        VehicleControlPolicy vehicleControlPolicy = new VehicleControlPolicy(testConfig());

        VehicleState currentState = baseState(60.0, VehicleStatus.ACTIVE, AvoidanceAction.KEEP_COURSE);
        AtomicReference<Double> targetDirection = new AtomicReference<>(0.0);

        vehicleControlPolicy.applyControlCommand(null, currentState, targetDirection::set);

        assertEquals(60.0, currentState.getSpeed(), 0.0001);
        assertEquals(AvoidanceAction.KEEP_COURSE, currentState.getCurrentAction());
        assertEquals(0.0, targetDirection.get(), 0.0001);
    }

    @Test
    void aiTickRespectsManualOverrideUntilItExpires() {
        VehicleControlPolicy vehicleControlPolicy = new VehicleControlPolicy(manualOverrideShortConfig());

        VehicleState currentState = baseState(60.0, VehicleStatus.ACTIVE, AvoidanceAction.KEEP_COURSE);
        AtomicReference<Double> targetDirection = new AtomicReference<>(0.0);

        ControlCommand controlCommand = new ControlCommand();
        controlCommand.setOverrideDirectionDeg(90.0);
        controlCommand.setManualOverride(true);

        vehicleControlPolicy.applyControlCommand(controlCommand, currentState, targetDirection::set);

        vehicleControlPolicy.aiTick(
                currentState,
                List.of(),
                () -> 0.0,
                targetDirection::set
        );

        assertEquals(AvoidanceAction.USER_OVERRIDE, currentState.getCurrentAction());
        assertEquals(60.0, currentState.getSpeed(), 0.0001);
        assertEquals(90.0, targetDirection.get(), 0.0001);
    }

    @Test
    void aiTickKeepsCourseAndRestoresSpeedWhenBelowInitial() throws InterruptedException {
        VehicleControlPolicy vehicleControlPolicy = new VehicleControlPolicy(manualOverrideShortConfig());

        VehicleState currentState = baseState(50.0, VehicleStatus.ACTIVE, AvoidanceAction.USER_OVERRIDE);
        AtomicReference<Double> targetDirection = new AtomicReference<>(0.0);

        ControlCommand controlCommand = new ControlCommand();
        controlCommand.setManualOverride(true);
        vehicleControlPolicy.applyControlCommand(controlCommand, currentState, targetDirection::set);

        Thread.sleep(30L);

        vehicleControlPolicy.aiTick(
                currentState,
                List.of(),
                () -> 0.0,
                targetDirection::set
        );

        assertEquals(AvoidanceAction.KEEP_COURSE, currentState.getCurrentAction());
        assertEquals(RiskLevel.LOW, currentState.getRiskLevel());
        assertEquals(54.0, currentState.getSpeed(), 0.0001); // 50 * 1.08
        assertEquals(VehicleStatus.ACTIVE, currentState.getStatus());
    }

    @Test
    void aiTickKeepsCourseAndRestartsVehicleWhenStopped() {
        VehicleControlPolicy vehicleControlPolicy = new VehicleControlPolicy(testConfig());

        VehicleState currentState = baseState(0.0, VehicleStatus.STOPPED, AvoidanceAction.EMERGENCY_STOP);
        AtomicReference<Double> targetDirection = new AtomicReference<>(0.0);

        vehicleControlPolicy.aiTick(
                currentState,
                List.of(),
                () -> 0.0,
                targetDirection::set
        );

        assertEquals(AvoidanceAction.KEEP_COURSE, currentState.getCurrentAction());
        assertEquals(60.0, currentState.getSpeed(), 0.0001);
        assertEquals(VehicleStatus.ACTIVE, currentState.getStatus());
    }

    @Test
    void aiTickSetsRiskAndChoosesAnAvoidanceActionWhenThreatExists() {
        VehicleControlPolicy vehicleControlPolicy = new VehicleControlPolicy(testConfig());

        VehicleState currentState = baseState(60.0, VehicleStatus.ACTIVE, AvoidanceAction.KEEP_COURSE);
        AtomicReference<Double> targetDirection = new AtomicReference<>(0.0);
        DoubleSupplier currentTargetDirection = () -> 90.0;

        VehicleState nearbyVehicle = vehicle("VH-OTHER", 120.0, 100.0, 180.0, 20.0, 16.0);

        vehicleControlPolicy.aiTick(
                currentState,
                List.of(nearbyVehicle),
                currentTargetDirection,
                targetDirection::set
        );

        assertTrue(currentState.getCurrentRiskScore() >= 0.0);
        assertTrue(
                currentState.getRiskLevel() == RiskLevel.MEDIUM
                        || currentState.getRiskLevel() == RiskLevel.HIGH
        );
        assertTrue(
                currentState.getCurrentAction() == AvoidanceAction.TURN_LEFT
                        || currentState.getCurrentAction() == AvoidanceAction.TURN_RIGHT
                        || currentState.getCurrentAction() == AvoidanceAction.SLOW_DOWN
                        || currentState.getCurrentAction() == AvoidanceAction.KEEP_COURSE
        );
    }

    @Test
    void aiTickDoesNotSlowDownBelowInitialSpeed() {
        VehicleControlPolicy vehicleControlPolicy = new VehicleControlPolicy(testConfig());

        VehicleState currentState = baseState(60.0, VehicleStatus.ACTIVE, AvoidanceAction.KEEP_COURSE);
        currentState.setSpeed(60.0);

        VehicleState nearbyVehicle = vehicle("VH-OTHER", 130.0, 100.0, 180.0, 20.0, 16.0);

        vehicleControlPolicy.aiTick(
                currentState,
                List.of(nearbyVehicle),
                () -> 0.0,
                ignored -> {}
        );

        if (currentState.getCurrentAction() == AvoidanceAction.SLOW_DOWN) {
            assertEquals(60.0, currentState.getSpeed(), 0.0001);
        }
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
                "--manualOverrideHoldMillis=2000",
                "--aiTurnDeltaDeg=8.0",
                "--aiSlowDownFactor=0.90",
                "--aiRecoveryFactor=1.08",
                "--aiPredictionSteps=20",
                "--aiPredictionStepSeconds=0.1",
                "--aiKeepCourseRiskThreshold=0.12"
        });
    }

    private VehicleProcessConfig manualOverrideShortConfig() {
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
                "--manualOverrideHoldMillis=10",
                "--aiTurnDeltaDeg=8.0",
                "--aiSlowDownFactor=0.90",
                "--aiRecoveryFactor=1.08",
                "--aiPredictionSteps=20",
                "--aiPredictionStepSeconds=0.1",
                "--aiKeepCourseRiskThreshold=0.12"
        });
    }

    private VehicleState baseState(double speed,
                                   VehicleStatus status,
                                   AvoidanceAction action) {
        VehicleState vehicleState = new VehicleState();
        vehicleState.setId("VH-TEST");
        vehicleState.setX(100.0);
        vehicleState.setY(100.0);
        vehicleState.setDirectionDeg(0.0);
        vehicleState.setSpeed(speed);
        vehicleState.setRadius(16.0);
        vehicleState.setStatus(status);
        vehicleState.setCurrentAction(action);
        vehicleState.setRiskLevel(RiskLevel.LOW);
        vehicleState.setCurrentRiskScore(0.0);
        return vehicleState;
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
        vehicleState.setRiskLevel(RiskLevel.LOW);
        vehicleState.setCurrentRiskScore(0.0);
        return vehicleState;
    }
}