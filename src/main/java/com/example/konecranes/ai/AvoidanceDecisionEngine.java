package com.example.konecranes.ai;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Selects the lowest-risk maneuver from a small set of candidate actions.
 */
public class AvoidanceDecisionEngine {

    private final RiskEstimator riskEstimator;
    private final double keepCourseRiskThreshold;

    public AvoidanceDecisionEngine(RiskEstimator riskEstimator, double keepCourseRiskThreshold) {
        this.riskEstimator = riskEstimator;
        this.keepCourseRiskThreshold = keepCourseRiskThreshold;
    }

    /**
     * Chooses a control action for the current vehicle state and nearby context.
     *
     * @param self current vehicle snapshot
     * @param nearbyVehicles nearby vehicle snapshots
     * @return selected action plus risk metadata
     */
    public DecisionResult choose(VehicleState self, List<VehicleState> nearbyVehicles) {
        if (nearbyVehicles.isEmpty()) {
            return new DecisionResult(AvoidanceAction.KEEP_COURSE, 0.0, RiskLevel.LOW);
        }

        Candidate best = Arrays.asList(
                        candidate(self, nearbyVehicles, AvoidanceAction.KEEP_COURSE, 0.0, 1.0),
                        candidate(self, nearbyVehicles, AvoidanceAction.TURN_LEFT, -8.0, 1.0),
                        candidate(self, nearbyVehicles, AvoidanceAction.TURN_RIGHT, 8.0, 1.0),
                        candidate(self, nearbyVehicles, AvoidanceAction.SLOW_DOWN, 0.0, 0.82))
                .stream()
                .min(Comparator.comparingDouble(Candidate::getScore))
                .orElseThrow(IllegalStateException::new);

        RiskAssessment current = riskEstimator.assess(self, nearbyVehicles);
        AvoidanceAction action = current.getRiskScore() < keepCourseRiskThreshold ? AvoidanceAction.KEEP_COURSE : best.getAction();
        return new DecisionResult(action, current.getRiskScore(), current.getRiskLevel());
    }

    private Candidate candidate(VehicleState self, List<VehicleState> nearbyVehicles, AvoidanceAction action,
                                double directionDelta, double speedFactor) {
        VehicleState scenario = self.copy();
        scenario.setDirectionDeg(normalize(scenario.getDirectionDeg() + directionDelta));
        scenario.setSpeed(Math.max(0.0, scenario.getSpeed() * speedFactor));
        double totalRisk = 0.0;
        for (VehicleState other : nearbyVehicles) {
            if (!scenario.getId().equals(other.getId())) {
                totalRisk += riskEstimator.pairwiseRisk(scenario, other);
            }
        }
        totalRisk += maneuverPenalty(action);
        return new Candidate(action, totalRisk);
    }

    private double maneuverPenalty(AvoidanceAction action) {
        switch (action) {
            case TURN_LEFT:
            case TURN_RIGHT:
                return 0.04;
            case SLOW_DOWN:
                return 0.03;
            case EMERGENCY_STOP:
                return 0.08;
            default:
                return 0.0;
        }
    }

    private double normalize(double value) {
        double normalized = value % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }

    private static class Candidate {
        private final AvoidanceAction action;
        private final double score;

        private Candidate(AvoidanceAction action, double score) {
            this.action = action;
            this.score = score;
        }

        public AvoidanceAction getAction() {
            return action;
        }

        public double getScore() {
            return score;
        }
    }

    /**
     * Decision payload returned by {@link #choose(VehicleState, List)}.
     */
    public static class DecisionResult {
        private final AvoidanceAction action;
        private final double riskScore;
        private final RiskLevel riskLevel;

        /**
         * @param action chosen maneuver
         * @param riskScore current aggregated risk score
         * @param riskLevel current risk level bucket
         */
        public DecisionResult(AvoidanceAction action, double riskScore, RiskLevel riskLevel) {
            this.action = action;
            this.riskScore = riskScore;
            this.riskLevel = riskLevel;
        }

        /** @return chosen maneuver */
        public AvoidanceAction getAction() {
            return action;
        }

        /** @return aggregated risk score */
        public double getRiskScore() {
            return riskScore;
        }

        /** @return risk level bucket */
        public RiskLevel getRiskLevel() {
            return riskLevel;
        }
    }
}
