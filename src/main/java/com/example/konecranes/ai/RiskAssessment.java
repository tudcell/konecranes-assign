package com.example.konecranes.ai;

import com.example.konecranes.model.RiskLevel;
import lombok.Getter;

/**
 * Value object containing numeric and categorical collision risk outputs.
 */
@Getter
public class RiskAssessment {
    /**
     * @return normalized risk score
     */
    private final double riskScore;
    /**
     * @return coarse risk level
     */
    private final RiskLevel riskLevel;

    /**
     * @param riskScore normalized risk score
     * @param riskLevel coarse risk level
     */
    public RiskAssessment(double riskScore, RiskLevel riskLevel) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
    }

}
