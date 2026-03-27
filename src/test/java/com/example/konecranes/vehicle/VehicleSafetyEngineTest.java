package com.example.konecranes.vehicle;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class VehicleSafetyEngineTest {

    @Test
    void findImmediateThreatReturnsNullWhenNoThreatExists() {
        VehicleSafetyEngine vehicleSafetyEngine = new VehicleSafetyEngine(testConfig());

        VehicleState currentState = vehicle("VH-SELF", 100.0, 100.0, 0.0, 50.0, 16.0);
        VehicleState farVehicle = vehicle("VH-OTHER", 400.0, 400.0, 180.0, 50.0, 16.0);

        VehicleState threat = vehicleSafetyEngine.findImmediateThreat(
                currentState,
                List.of(farVehicle),
                105.0,
                100.0
        );

        assertNull(threat);
    }

    @Test
    void findImmediateThreatIgnoresSelfVehicle() {
        VehicleSafetyEngine vehicleSafetyEngine = new VehicleSafetyEngine(testConfig());

        VehicleState currentState = vehicle("VH-SELF", 100.0, 100.0, 0.0, 50.0, 16.0);

        VehicleState threat = vehicleSafetyEngine.findImmediateThreat(
                currentState,
                List.of(currentState),
                105.0,
                100.0
        );

        assertNull(threat);
    }

    @Test
    void findImmediateThreatReturnsNearestThreat() {
        VehicleSafetyEngine vehicleSafetyEngine = new VehicleSafetyEngine(testConfig());

        VehicleState currentState = vehicle("VH-SELF", 100.0, 100.0, 0.0, 50.0, 16.0);
        VehicleState nearestVehicle = vehicle("VH-NEAR", 135.0, 100.0, 180.0, 10.0, 16.0);
        VehicleState fartherVehicle = vehicle("VH-FAR", 150.0, 100.0, 180.0, 10.0, 16.0);

        VehicleState threat = vehicleSafetyEngine.findImmediateThreat(
                currentState,
                List.of(fartherVehicle, nearestVehicle),
                120.0,
                100.0
        );

        assertNotNull(threat);
        assertEquals("VH-NEAR", threat.getId());
    }

    @Test
    void findImmediateThreatUsesLookaheadPrediction() {
        VehicleSafetyEngine vehicleSafetyEngine = new VehicleSafetyEngine(testConfig());

        VehicleState currentState = vehicle("VH-SELF", 100.0, 100.0, 0.0, 50.0, 16.0);
        VehicleState nearbyVehicle = vehicle("VH-OTHER", 140.0, 100.0, 180.0, 50.0, 16.0);

        VehicleState threat = vehicleSafetyEngine.findImmediateThreat(
                currentState,
                List.of(nearbyVehicle),
                104.0,
                100.0
        );

        assertNotNull(threat);
        assertEquals("VH-OTHER", threat.getId());
    }

    @Test
    void applyEmergencyManeuverPerformsHardStopWhenSeparationIsCritical() {
        VehicleSafetyEngine vehicleSafetyEngine = new VehicleSafetyEngine(testConfig());

        VehicleState currentState = vehicle("VH-SELF", 100.0, 100.0, 0.0, 80.0, 16.0);
        currentState.setStatus(VehicleStatus.ACTIVE);
        currentState.setCurrentAction(AvoidanceAction.KEEP_COURSE);

        VehicleState threatVehicle = vehicle("VH-OTHER", 120.0, 100.0, 180.0, 20.0, 16.0);

        AtomicReference<Double> targetDirection = new AtomicReference<>();

        vehicleSafetyEngine.applyEmergencyManeuver(
                currentState,
                threatVehicle,
                targetDirection::set
        );

        assertEquals(20.0, currentState.getSpeed(), 0.0001);
        assertEquals(VehicleStatus.STOPPED, currentState.getStatus());
        assertEquals(AvoidanceAction.EMERGENCY_STOP, currentState.getCurrentAction());
        assertNotNull(targetDirection.get());
        assertEquals(180.0, normalizeDegrees(targetDirection.get()), 0.0001);
    }

    @Test
    void applyEmergencyManeuverPerformsSoftBrakeWhenThreatIsClose() {
        VehicleSafetyEngine vehicleSafetyEngine = new VehicleSafetyEngine(testConfig());

        VehicleState currentState = vehicle("VH-SELF", 100.0, 100.0, 0.0, 80.0, 16.0);
        currentState.setStatus(VehicleStatus.ACTIVE);
        currentState.setCurrentAction(AvoidanceAction.KEEP_COURSE);

        VehicleState threatVehicle = vehicle("VH-OTHER", 140.0, 100.0, 180.0, 20.0, 16.0);

        AtomicReference<Double> targetDirection = new AtomicReference<>();

        vehicleSafetyEngine.applyEmergencyManeuver(
                currentState,
                threatVehicle,
                targetDirection::set
        );

        assertEquals(64.0, currentState.getSpeed(), 0.0001);
        assertEquals(VehicleStatus.ACTIVE, currentState.getStatus());
        assertEquals(AvoidanceAction.SLOW_DOWN, currentState.getCurrentAction());
        assertNotNull(targetDirection.get());
        assertEquals(180.0, normalizeDegrees(targetDirection.get()), 0.0001);
    }

    @Test
    void applyEmergencyManeuverRespectsSoftBrakeMinimumSpeed() {
        VehicleSafetyEngine vehicleSafetyEngine = new VehicleSafetyEngine(testConfig());

        VehicleState currentState = vehicle("VH-SELF", 100.0, 100.0, 0.0, 10.0, 16.0);
        VehicleState threatVehicle = vehicle("VH-OTHER", 140.0, 100.0, 180.0, 20.0, 16.0);

        AtomicReference<Double> targetDirection = new AtomicReference<>();

        vehicleSafetyEngine.applyEmergencyManeuver(
                currentState,
                threatVehicle,
                targetDirection::set
        );

        assertEquals(20.0, currentState.getSpeed(), 0.0001);
        assertEquals(VehicleStatus.ACTIVE, currentState.getStatus());
        assertEquals(AvoidanceAction.SLOW_DOWN, currentState.getCurrentAction());
    }

    @Test
    void applyEmergencyManeuverKeepsCourseWhenThreatIsOutsideBrakeZones() {
        VehicleSafetyEngine vehicleSafetyEngine = new VehicleSafetyEngine(testConfig());

        VehicleState currentState = vehicle("VH-SELF", 100.0, 100.0, 0.0, 80.0, 16.0);
        currentState.setStatus(VehicleStatus.STOPPED);
        currentState.setCurrentAction(AvoidanceAction.EMERGENCY_STOP);

        VehicleState threatVehicle = vehicle("VH-OTHER", 200.0, 100.0, 180.0, 20.0, 16.0);

        AtomicReference<Double> targetDirection = new AtomicReference<>();

        vehicleSafetyEngine.applyEmergencyManeuver(
                currentState,
                threatVehicle,
                targetDirection::set
        );

        assertEquals(80.0, currentState.getSpeed(), 0.0001);
        assertEquals(VehicleStatus.ACTIVE, currentState.getStatus());
        assertEquals(AvoidanceAction.KEEP_COURSE, currentState.getCurrentAction());
        assertNull(targetDirection.get());
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
                "--initialSpeed=50",
                "--tickMillis=100",
                "--safetyEmergencyMargin=3.0",
                "--safetyEmergencyLookaheadSeconds=0.08",
                "--safetyHardStopFactor=0.25",
                "--safetySoftBrakeFactor=0.80",
                "--safetySoftBrakeMinimumSpeed=20.0"
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

    private double normalizeDegrees(double degrees) {
        double normalized = degrees % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }
}