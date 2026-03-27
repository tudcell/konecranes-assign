package com.example.konecranes.ai;

import com.example.konecranes.model.RiskLevel;
import lombok.Getter;

/**
 * Immutable result of one risk evaluation.
 *
 * Contains both:
 * - the numeric risk score
 * - the categorical risk level derived from that score
 */
@Getter
public class RiskAssessment {

    private final double riskScore;
    private final RiskLevel riskLevel;

    /**
     * Creates a new risk assessment result.
     *
     * @param riskScore normalized numeric risk score
     * @param riskLevel categorical risk bucket
     */
    public RiskAssessment(double riskScore, RiskLevel riskLevel) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
    }
}