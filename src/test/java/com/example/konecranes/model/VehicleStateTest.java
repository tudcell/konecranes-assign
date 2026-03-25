package com.example.konecranes.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehicleStateTest {
    @Test
    void copyCreatesDeepEnoughCopy() {
        VehicleState original = new VehicleState();
        original.setId("v1");
        original.setX(10.0);
        original.setY(20.0);
        original.setDirectionDeg(90.0);
        original.setSpeed(50.0);
        original.setRadius(16.0);
        original.setTimestamp(123456789L);
        original.setStatus(VehicleStatus.ACTIVE);
        original.setCurrentAction(AvoidanceAction.KEEP_COURSE);
        original.setRiskLevel(RiskLevel.LOW);
        original.setCurrentRiskScore(0.42);

        VehicleState copy = original.copy();

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getX(), copy.getX());
        assertEquals(original.getY(), copy.getY());
        assertEquals(original.getDirectionDeg(), copy.getDirectionDeg());
        assertEquals(original.getSpeed(), copy.getSpeed());
        assertEquals(original.getRadius(), copy.getRadius());
        assertEquals(original.getTimestamp(), copy.getTimestamp());
        assertEquals(original.getStatus(), copy.getStatus());
        assertEquals(original.getCurrentAction(), copy.getCurrentAction());
        assertEquals(original.getRiskLevel(), copy.getRiskLevel());
        assertEquals(original.getCurrentRiskScore(), copy.getCurrentRiskScore());
        assertNotSame(original, copy);
    }
}

