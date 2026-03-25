package com.example.konecranes.model;

/**
 * High-level action chosen by control/safety logic for a vehicle tick.
 */
public enum AvoidanceAction {
    /** Keep current heading/speed profile. */
    KEEP_COURSE,
    /** Steer left relative to current heading. */
    TURN_LEFT,
    /** Steer right relative to current heading. */
    TURN_RIGHT,
    /** Reduce speed while remaining active. */
    SLOW_DOWN,
    /** Stop immediately because danger is imminent. */
    EMERGENCY_STOP,
    /** Manual command from user currently overrides AI decisions. */
    USER_OVERRIDE
}
