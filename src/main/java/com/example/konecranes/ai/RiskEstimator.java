package com.example.konecranes.ai;

import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;

import java.util.List;

/**
 * Estimates collision risk for one vehicle against nearby traffic.
 *
 * Produces:
 * - a numeric risk score
 * - a categorical risk level
 *
 * The estimator combines current distance, predicted future distance,
 * closing behavior, and heading alignment into one score.
 */
public class RiskEstimator {

    // Internal constants used by the risk formula.
    private static final double DANGER_DISTANCE_MARGIN = 18.0;
    private static final double RELATIVE_SPEED_FACTOR = 0.10;
    private static final double PROXIMITY_SAFE_MAX = 220.0;
    private static final double FUTURE_SAFE_MAX = 180.0;

    // Risk level thresholds.
    private static final double RISK_LEVEL_HIGH = 0.82;
    private static final double RISK_LEVEL_MEDIUM = 0.50;

    private final int predictionSteps;
    private final double dtSeconds;

    public RiskEstimator(int predictionSteps, double dtSeconds) {
        this.predictionSteps = predictionSteps;
        this.dtSeconds = dtSeconds;
    }

    /**
     * Computes the overall risk for one vehicle against nearby vehicles.
     *
     * The returned risk is the maximum pairwise risk found in the nearby set.
     *
     * @param self current vehicle
     * @param nearbyVehicles nearby vehicle states
     * @return aggregated risk assessment
     */
    public RiskAssessment assess(VehicleState self, List<VehicleState> nearbyVehicles) {
        double maxRisk = 0.0;

        for (VehicleState other : nearbyVehicles) {
            if (self.getId().equals(other.getId())) {
                continue;
            }
            maxRisk = Math.max(maxRisk, pairwiseRisk(self, other));
        }

        return new RiskAssessment(maxRisk, toLevel(maxRisk));
    }

    /**
     * Computes the pairwise collision risk between two vehicles.
     *
     * The score combines:
     * - current proximity
     * - predicted minimum future distance
     * - closing behavior
     * - heading convergence
     *
     * @param a first vehicle
     * @param b second vehicle
     * @return normalized risk score in range [0, 1]
     */
    public double pairwiseRisk(VehicleState a, VehicleState b) {
        double distanceNow = distance(a.getX(), a.getY(), b.getX(), b.getY());
        double minPredictedDistance = predictedMinimumDistance(a, b);
        double closingFactor = closingFactor(a, b);
        double intersectionFactor = headingConvergence(a, b);
        double relativeSpeed = Math.abs(a.getSpeed() - b.getSpeed());

        double dangerDistance =
                a.getRadius() + b.getRadius()
                        + DANGER_DISTANCE_MARGIN
                        + (relativeSpeed * RELATIVE_SPEED_FACTOR);

        double proximityScore = inverseNormalize(distanceNow, dangerDistance, PROXIMITY_SAFE_MAX);
        double futureScore = inverseNormalize(minPredictedDistance, dangerDistance, FUTURE_SAFE_MAX);

        double risk = 0.30 * proximityScore
                + 0.40 * futureScore
                + 0.20 * closingFactor
                + 0.10 * intersectionFactor;

        return clamp(risk, 0.0, 1.0);
    }

    /**
     * Maps a numeric score to a categorical risk level.
     *
     * @param score numeric risk score
     * @return low, medium, or high risk level
     */
    private RiskLevel toLevel(double score) {
        if (score >= RISK_LEVEL_HIGH) {
            return RiskLevel.HIGH;
        }
        if (score >= RISK_LEVEL_MEDIUM) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    /**
     * Predicts the minimum distance between two vehicles
     * over the configured lookahead window.
     *
     * @param a first vehicle
     * @param b second vehicle
     * @return minimum predicted distance
     */
    private double predictedMinimumDistance(VehicleState a, VehicleState b) {
        double ax = a.getX();
        double ay = a.getY();
        double bx = b.getX();
        double by = b.getY();

        double ar = Math.toRadians(a.getDirectionDeg());
        double br = Math.toRadians(b.getDirectionDeg());

        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < predictionSteps; i++) {
            ax += Math.cos(ar) * a.getSpeed() * dtSeconds;
            ay += Math.sin(ar) * a.getSpeed() * dtSeconds;
            bx += Math.cos(br) * b.getSpeed() * dtSeconds;
            by += Math.sin(br) * b.getSpeed() * dtSeconds;
            minDistance = Math.min(minDistance, distance(ax, ay, bx, by));
        }

        return minDistance;
    }

    /**
     * Estimates how strongly two vehicles are moving toward each other.
     *
     * @param a first vehicle
     * @param b second vehicle
     * @return normalized closing factor in range [0, 1]
     */
    private double closingFactor(VehicleState a, VehicleState b) {
        double relPosX = b.getX() - a.getX();
        double relPosY = b.getY() - a.getY();

        double avx = Math.cos(Math.toRadians(a.getDirectionDeg())) * a.getSpeed();
        double avy = Math.sin(Math.toRadians(a.getDirectionDeg())) * a.getSpeed();
        double bvx = Math.cos(Math.toRadians(b.getDirectionDeg())) * b.getSpeed();
        double bvy = Math.sin(Math.toRadians(b.getDirectionDeg())) * b.getSpeed();

        double relVelX = bvx - avx;
        double relVelY = bvy - avy;

        double closing = -1.0 * (relPosX * relVelX + relPosY * relVelY);
        return clamp(closing / 5000.0, 0.0, 1.0);
    }

    /**
     * Measures how much two headings are converging.
     *
     * Vehicles with more similar heading angles produce a higher value.
     *
     * @param a first vehicle
     * @param b second vehicle
     * @return normalized convergence factor in range [0, 1]
     */
    private double headingConvergence(VehicleState a, VehicleState b) {
        double angle = Math.abs(normalizeAngle(a.getDirectionDeg() - b.getDirectionDeg()));
        double convergence = 1.0 - (Math.min(angle, 180.0) / 180.0);
        return clamp(convergence, 0.0, 1.0);
    }

    /**
     * Inverse-normalizes a value into the range [0, 1].
     *
     * Values near safeMin map closer to 1.0.
     * Values near or above safeMax map closer to 0.0.
     *
     * @param value input value
     * @param safeMin lower bound
     * @param safeMax upper bound
     * @return normalized score
     */
    private double inverseNormalize(double value, double safeMin, double safeMax) {
        if (value <= safeMin) {
            return 1.0;
        }
        if (value >= safeMax) {
            return 0.0;
        }
        return 1.0 - ((value - safeMin) / (safeMax - safeMin));
    }

    /**
     * Computes Euclidean distance between two points.
     *
     * @param x1 first point x
     * @param y1 first point y
     * @param x2 second point x
     * @param y2 second point y
     * @return distance
     */
    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    /**
     * Normalizes an angle into the range [-180, 180].
     *
     * @param degrees input angle
     * @return normalized angle
     */
    private double normalizeAngle(double degrees) {
        double normalized = degrees % 360.0;
        if (normalized > 180.0) {
            normalized -= 360.0;
        }
        if (normalized < -180.0) {
            normalized += 360.0;
        }
        return normalized;
    }

    /**
     * Clamps a value into the given range.
     *
     * @param value input value
     * @param min lower bound
     * @param max upper bound
     * @return clamped value
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}