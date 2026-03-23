package com.example.konecranes.ai;

import com.example.konecranes.model.RiskLevel;
import com.example.konecranes.model.VehicleState;

import java.util.List;

public class RiskEstimator {

    private final int predictionSteps;
    private final double dtSeconds;

    public RiskEstimator(int predictionSteps, double dtSeconds) {
        this.predictionSteps = predictionSteps;
        this.dtSeconds = dtSeconds;
    }

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

    public double pairwiseRisk(VehicleState a, VehicleState b) {
        double distanceNow = distance(a.getX(), a.getY(), b.getX(), b.getY());
        double minPredictedDistance = predictedMinimumDistance(a, b);
        double closingFactor = closingFactor(a, b);
        double intersectionFactor = headingConvergence(a, b);

        double dangerDistance = a.getRadius() + b.getRadius() + 20.0;
        double proximityScore = inverseNormalize(distanceNow, dangerDistance, 300.0);
        double futureScore = inverseNormalize(minPredictedDistance, dangerDistance, 220.0);

        double risk = 0.35 * proximityScore
                + 0.35 * futureScore
                + 0.20 * closingFactor
                + 0.10 * intersectionFactor;

        return clamp(risk, 0.0, 1.0);
    }

    private RiskLevel toLevel(double score) {
        if (score >= 0.75) {
            return RiskLevel.HIGH;
        }
        if (score >= 0.4) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

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

    private double headingConvergence(VehicleState a, VehicleState b) {
        double angle = Math.abs(normalizeAngle(a.getDirectionDeg() - b.getDirectionDeg()));
        double convergence = 1.0 - (Math.min(angle, 180.0) / 180.0);
        return clamp(convergence, 0.0, 1.0);
    }

    private double inverseNormalize(double value, double safeMin, double safeMax) {
        if (value <= safeMin) {
            return 1.0;
        }
        if (value >= safeMax) {
            return 0.0;
        }
        return 1.0 - ((value - safeMin) / (safeMax - safeMin));
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

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

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
