package com.example.konecranes.ai;

import com.example.konecranes.model.RiskLevel;

public class RiskAssessment {
    private final double riskScore;
    private final RiskLevel riskLevel;

    public RiskAssessment(double riskScore, RiskLevel riskLevel) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
}
