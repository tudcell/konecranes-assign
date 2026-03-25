package com.example.konecranes.ai;

import com.example.konecranes.model.RiskLevel;

/**
 * Value object containing numeric and categorical collision risk outputs.
 */
public class RiskAssessment {
    private final double riskScore;
    private final RiskLevel riskLevel;

    /**
     * @param riskScore normalized risk score
     * @param riskLevel coarse risk level
     */
    public RiskAssessment(double riskScore, RiskLevel riskLevel) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
    }

    /** @return normalized risk score */
    public double getRiskScore() {
        return riskScore;
    }

    /** @return coarse risk level */
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
}
