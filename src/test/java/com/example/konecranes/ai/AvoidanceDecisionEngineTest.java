package com.example.konecranes.ai;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AvoidanceDecisionEngineTest {

    @Test
    void chooseReturnsKeepCourseWhenNoNearbyVehicles() {
        RiskEstimator estimator = mock(RiskEstimator.class);
        AvoidanceDecisionEngine engine = new AvoidanceDecisionEngine(estimator, 0.12);

        AvoidanceDecisionEngine.DecisionResult result = engine.choose(vehicle("v1", 0, 0, 0, 50), Collections.emptyList());

        assertEquals(AvoidanceAction.KEEP_COURSE, result.getAction());
        assertEquals(0.0, result.getRiskScore(), 0.0001);
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
    }

    @Test
    void choosePrefersBestCandidateWhenCurrentRiskIsAboveThreshold() {
        RiskEstimator estimator = mock(RiskEstimator.class);
        when(estimator.assess(any(VehicleState.class), any())).thenReturn(new RiskAssessment(0.85, RiskLevel.HIGH));
        when(estimator.pairwiseRisk(any(VehicleState.class), any(VehicleState.class))).thenAnswer(invocation -> {
            VehicleState scenario = invocation.getArgument(0);
            if (scenario.getSpeed() < 100.0) {
                return 0.12; // SLOW_DOWN candidate
            }
            if (Math.abs(scenario.getDirectionDeg() - 0.0) > 0.001) {
                return 0.40; // TURN candidates
            }
            return 0.60; // KEEP_COURSE candidate
        });

        AvoidanceDecisionEngine engine = new AvoidanceDecisionEngine(estimator, 0.12);
        VehicleState self = vehicle("v1", 100, 100, 0, 100);
        VehicleState other = vehicle("v2", 140, 100, 180, 80);

        AvoidanceDecisionEngine.DecisionResult result = engine.choose(self, Collections.singletonList(other));

        assertEquals(AvoidanceAction.SLOW_DOWN, result.getAction());
        assertEquals(0.85, result.getRiskScore(), 0.0001);
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
    }

    @Test
    void chooseKeepsCourseWhenBelowConfiguredThreshold() {
        RiskEstimator estimator = mock(RiskEstimator.class);
        when(estimator.assess(any(VehicleState.class), any())).thenReturn(new RiskAssessment(0.05, RiskLevel.LOW));
        when(estimator.pairwiseRisk(any(VehicleState.class), any(VehicleState.class))).thenReturn(0.9);

        AvoidanceDecisionEngine engine = new AvoidanceDecisionEngine(estimator, 0.12);
        VehicleState self = vehicle("v1", 100, 100, 0, 100);
        VehicleState other = vehicle("v2", 130, 100, 180, 80);

        AvoidanceDecisionEngine.DecisionResult result = engine.choose(self, Collections.singletonList(other));

        assertEquals(AvoidanceAction.KEEP_COURSE, result.getAction());
        assertEquals(0.05, result.getRiskScore(), 0.0001);
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
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

