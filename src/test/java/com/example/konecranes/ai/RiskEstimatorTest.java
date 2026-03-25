package com.example.konecranes.ai;

import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskEstimatorTest {

    private final RiskEstimator estimator = new RiskEstimator(20, 0.1);

    @Test
    void assessReturnsLowRiskWhenOnlySelfIsInContext() {
        VehicleState self = vehicle("self", 100, 100, 0, 50);

        RiskAssessment assessment = estimator.assess(self, Collections.singletonList(self.copy()));

        assertEquals(0.0, assessment.getRiskScore(), 0.0001);
        assertEquals(RiskLevel.LOW, assessment.getRiskLevel());
    }

    @Test
    void pairwiseRiskIsHigherForCloseHeadOnTraffic() {
        VehicleState self = vehicle("v1", 100, 100, 0, 55);
        VehicleState closeHeadOn = vehicle("v2", 140, 100, 180, 55);
        VehicleState farParallel = vehicle("v3", 500, 500, 0, 20);

        double highRisk = estimator.pairwiseRisk(self, closeHeadOn);
        double lowRisk = estimator.pairwiseRisk(self, farParallel);

        assertTrue(highRisk > lowRisk);
        assertTrue(highRisk >= 0.0 && highRisk <= 1.0);
        assertTrue(lowRisk >= 0.0 && lowRisk <= 1.0);
    }

    @Test
    void assessUsesMaximumPairwiseRiskAcrossNearbyVehicles() {
        VehicleState self = vehicle("v1", 100, 100, 0, 60);
        VehicleState mediumThreat = vehicle("v2", 220, 100, 180, 50);
        VehicleState strongThreat = vehicle("v3", 130, 100, 180, 60);

        double mediumRisk = estimator.pairwiseRisk(self, mediumThreat);
        double strongRisk = estimator.pairwiseRisk(self, strongThreat);

        RiskAssessment assessment = estimator.assess(self, Arrays.asList(mediumThreat, strongThreat));

        assertEquals(Math.max(mediumRisk, strongRisk), assessment.getRiskScore(), 0.0001);
    }

    private VehicleState vehicle(String id, double x, double y, double directionDeg, double speed) {
        VehicleState state = new VehicleState();
        state.setId(id);
        state.setX(x);
        state.setY(y);
        state.setDirectionDeg(directionDeg);
        state.setSpeed(speed);
        state.setRadius(16.0);
        return state;
    }
}

