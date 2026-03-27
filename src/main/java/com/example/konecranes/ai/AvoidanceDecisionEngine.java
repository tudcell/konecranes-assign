package com.example.konecranes.ai;

import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Chooses the lowest-risk maneuver from a small set of candidate actions.
 *
 * The engine compares a few simple action scenarios and selects
 * the one with the lowest predicted total risk.
 */
public class AvoidanceDecisionEngine {

    // Small penalties used to avoid unnecessary maneuvering
    // when multiple actions have very similar risk.
    private static final double PENALTY_TURN = 0.04;
    private static final double PENALTY_SLOW_DOWN = 0.03;
    private static final double PENALTY_EMERGENCY_STOP = 0.08;

    // Candidate direction deltas used when simulating turns.
    private static final double DELTA_TURN_LEFT = -8.0;
    private static final double DELTA_TURN_RIGHT = 8.0;

    // Candidate speed multipliers used when simulating actions.
    private static final double FACTOR_KEEP_COURSE = 1.0;
    private static final double FACTOR_TURN = 1.0;
    private static final double FACTOR_SLOW_DOWN = 0.82;

    private final RiskEstimator riskEstimator;
    private final double keepCourseRiskThreshold;

    public AvoidanceDecisionEngine(RiskEstimator riskEstimator, double keepCourseRiskThreshold) {
        this.riskEstimator = riskEstimator;
        this.keepCourseRiskThreshold = keepCourseRiskThreshold;
    }

    /**
     * Chooses a control action for the current vehicle state.
     *
     * If the current risk is below the keep-course threshold,
     * the engine keeps the current maneuver even if another
     * simulated action is slightly safer.
     *
     * @param self current vehicle state
     * @param nearbyVehicles nearby vehicle states
     * @return selected action and current risk information
     */
    public DecisionResult choose(VehicleState self, List<VehicleState> nearbyVehicles) {
        if (nearbyVehicles.isEmpty()) {
            return new DecisionResult(AvoidanceAction.KEEP_COURSE, 0.0, RiskLevel.LOW);
        }

        Candidate best = Stream.of(
                        candidate(self, nearbyVehicles, AvoidanceAction.KEEP_COURSE, 0.0, FACTOR_KEEP_COURSE),
                        candidate(self, nearbyVehicles, AvoidanceAction.TURN_LEFT, DELTA_TURN_LEFT, FACTOR_TURN),
                        candidate(self, nearbyVehicles, AvoidanceAction.TURN_RIGHT, DELTA_TURN_RIGHT, FACTOR_TURN),
                        candidate(self, nearbyVehicles, AvoidanceAction.SLOW_DOWN, 0.0, FACTOR_SLOW_DOWN))
                .min(Comparator.comparingDouble(Candidate::getScore))
                .orElseThrow(IllegalStateException::new);

        RiskAssessment current = riskEstimator.assess(self, nearbyVehicles);
        AvoidanceAction action =
                current.getRiskScore() < keepCourseRiskThreshold
                        ? AvoidanceAction.KEEP_COURSE
                        : best.getAction();

        return new DecisionResult(action, current.getRiskScore(), current.getRiskLevel());
    }

    /**
     * Builds one candidate scenario by applying a direction and speed change
     * to a copy of the current state, then scoring its total pairwise risk.
     *
     * @param self current vehicle state
     * @param nearbyVehicles nearby vehicle states
     * @param action candidate action being tested
     * @param directionDelta direction adjustment in degrees
     * @param speedFactor speed multiplier
     * @return scored candidate action
     */
    private Candidate candidate(VehicleState self,
                                List<VehicleState> nearbyVehicles,
                                AvoidanceAction action,
                                double directionDelta,
                                double speedFactor) {
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

    /**
     * Returns a small penalty for disruptive actions.
     *
     * This helps prefer simpler maneuvers when two choices
     * have nearly identical predicted risk.
     *
     * @param action candidate action
     * @return maneuver penalty
     */
    private double maneuverPenalty(AvoidanceAction action) {
        switch (action) {
            case TURN_LEFT:
            case TURN_RIGHT:
                return PENALTY_TURN;
            case SLOW_DOWN:
                return PENALTY_SLOW_DOWN;
            case EMERGENCY_STOP:
                return PENALTY_EMERGENCY_STOP;
            default:
                return 0.0;
        }
    }

    /**
     * Normalizes an angle into the range [0, 360).
     *
     * @param value angle in degrees
     * @return normalized angle
     */
    private double normalize(double value) {
        double normalized = value % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }

    /**
     * Internal candidate result used during action comparison.
     */
    @Getter
    private static class Candidate {
        private final AvoidanceAction action;
        private final double score;

        private Candidate(AvoidanceAction action, double score) {
            this.action = action;
            this.score = score;
        }
    }

    /**
     * Result returned by the decision engine.
     *
     * Contains the chosen action plus the current risk assessment.
     */
    @Getter
    public static class DecisionResult {
        private final AvoidanceAction action;
        private final double riskScore;
        private final RiskLevel riskLevel;

        public DecisionResult(AvoidanceAction action, double riskScore, RiskLevel riskLevel) {
            this.action = action;
            this.riskScore = riskScore;
            this.riskLevel = riskLevel;
        }
    }
}