package com.example.konecranes.model;

/**
 * Coarse risk classification derived from numeric collision risk score.
 */
public enum RiskLevel {
    /** Low immediate risk. */
    LOW,
    /** Moderate risk; should steer or slow proactively. */
    MEDIUM,
    /** High risk; immediate evasive behavior expected. */
    HIGH
}
