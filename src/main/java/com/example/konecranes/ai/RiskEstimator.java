package com.example.konecranes.ai;

import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;

import java.util.List;

/**
 * Estimates collision risk for a vehicle against nearby traffic.
 */

public class RiskEstimator {
    // Risk calculation constants (not meant to be configured externally)
    private static final double DANGER_DISTANCE_MARGIN = 18.0;
    private static final double RELATIVE_SPEED_FACTOR = 0.10;
    private static final double PROXIMITY_SAFE_MAX = 220.0;
    private static final double FUTURE_SAFE_MAX = 180.0;

    // Risk level thresholds
    private static final double RISK_LEVEL_HIGH = 0.82;
    private static final double RISK_LEVEL_MEDIUM = 0.50;

    private final int predictionSteps;
    private final double dtSeconds;

    public RiskEstimator(int predictionSteps, double dtSeconds) {
        this.predictionSteps = predictionSteps;
        this.dtSeconds = dtSeconds;
    }

    /**
     * Computes aggregate risk and risk level from nearby vehicles.
     *
     * @param self current vehicle
     * @param nearbyVehicles nearby context vehicles
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
     * Computes pairwise risk score between two vehicles.
     *
     * @param a first vehicle
     * @param b second vehicle
     * @return normalized score in range [0, 1]
     */
    public double pairwiseRisk(VehicleState a, VehicleState b) {
        double distanceNow = distance(a.getX(), a.getY(), b.getX(), b.getY());
        double minPredictedDistance = predictedMinimumDistance(a, b);
        double closingFactor = closingFactor(a, b);
        double intersectionFactor = headingConvergence(a, b);
        double relativeSpeed = Math.abs(a.getSpeed() - b.getSpeed());

        double dangerDistance = a.getRadius() + b.getRadius() + DANGER_DISTANCE_MARGIN + (relativeSpeed * RELATIVE_SPEED_FACTOR);
        double proximityScore = inverseNormalize(distanceNow, dangerDistance, PROXIMITY_SAFE_MAX);
        double futureScore = inverseNormalize(minPredictedDistance, dangerDistance, FUTURE_SAFE_MAX);

        double risk = 0.30 * proximityScore
                + 0.40 * futureScore
                + 0.20 * closingFactor
                + 0.10 * intersectionFactor;

        return clamp(risk, 0.0, 1.0);
    }

    /**
     * Maps numeric risk score to categorical risk level.
     *
     * @param score numeric score in range [0, 1]
     * @return coarse risk level
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
     * Predicts minimum distance between two vehicles over next prediction window.
     *
     * @param a first vehicle
     * @param b second vehicle
     * @return minimum distance during lookahead period
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
     * Estimates closing velocity component between two vehicles.
     *
     * @param a first vehicle
     * @param b second vehicle
     * @return closing factor in range [0, 1]
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
     * Measures heading alignment between two vehicles.
     *
     * @param a first vehicle
     * @param b second vehicle
     * @return convergence factor in range [0, 1]
     */
    private double headingConvergence(VehicleState a, VehicleState b) {
        double angle = Math.abs(normalizeAngle(a.getDirectionDeg() - b.getDirectionDeg()));
        double convergence = 1.0 - (Math.min(angle, 180.0) / 180.0);
        return clamp(convergence, 0.0, 1.0);
    }

    /**
     * Inverse normalization: high values near safeMin map to 1.0, high values near safeMax map to 0.0.
     *
     * @param value input value
     * @param safeMin safe distance lower bound
     * @param safeMax safe distance upper bound
     * @return normalized score in range [0, 1]
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
     * Euclidean distance between two points.
     *
     * @param x1 first point X
     * @param y1 first point Y
     * @param x2 second point X
     * @param y2 second point Y
     * @return distance
     */
    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    /**
     * Normalizes angle to range [-180, 180] degrees.
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
     * Clamps value within [min, max] bounds.
     *
     * @param value input value
     * @param min minimum bound
     * @param max maximum bound
     * @return clamped value
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
