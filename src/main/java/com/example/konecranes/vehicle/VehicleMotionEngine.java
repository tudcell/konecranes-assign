package com.example.konecranes.vehicle;

import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles heading and boundary movement behavior.
 */
public class VehicleMotionEngine {

    private final VehicleProcessConfig config;
    private final AtomicLong targetDirectionDegTimes100 = new AtomicLong(0L);

    // Magic numbers extracted as constants
    private static final double REVERSE_ANGLE_DEG = 180.0;
    private static final double FULL_CIRCLE_DEG = 360.0;
    private static final double DEGREE_SCALE = 100.0;
    private static final double SIGNED_DELTA_MIN = -180.0;

    public VehicleMotionEngine(VehicleProcessConfig config, double initialDirectionDeg) {
        this.config = config;
        setTargetDirection(initialDirectionDeg);
    }

    /**
     * Rotates current heading toward target heading with bounded turn rate.
     *
     * @param state mutable self state
     */
    public void rotateTowardsTarget(VehicleState state) {
        double current = normalizeDirection(state.getDirectionDeg());
        double target = getTargetDirection();
        double delta = shortestSignedDeltaDeg(current, target);
        double boundedDelta = Math.max(-config.getMaxTurnDegPerTick(), Math.min(config.getMaxTurnDegPerTick(), delta));
        state.setDirectionDeg(normalizeDirection(current + boundedDelta));
    }

    /**
     * Applies world boundary reflection and speed/status recovery if needed.
     *
     * @param state mutable self state
     */
    public void bounceIfNeeded(VehicleState state) {
        double radius = state.getRadius();
        boolean bounced = false;

        if (state.getX() <= radius) {
            state.setX(radius);
            state.setDirectionDeg(normalizeDirection(REVERSE_ANGLE_DEG - state.getDirectionDeg()));
            bounced = true;
        } else if (state.getX() >= config.getWorldWidth() - radius) {
            state.setX(config.getWorldWidth() - radius);
            state.setDirectionDeg(normalizeDirection(REVERSE_ANGLE_DEG - state.getDirectionDeg()));
            bounced = true;
        }

        if (state.getY() <= radius) {
            state.setY(radius);
            state.setDirectionDeg(normalizeDirection(FULL_CIRCLE_DEG - state.getDirectionDeg()));
            bounced = true;
        } else if (state.getY() >= config.getWorldHeight() - radius) {
            state.setY(config.getWorldHeight() - radius);
            state.setDirectionDeg(normalizeDirection(FULL_CIRCLE_DEG - state.getDirectionDeg()));
            bounced = true;
        }

        if (bounced && state.getSpeed() <= 0.0) {
            state.setSpeed(config.getInitialSpeed());
            state.setStatus(VehicleStatus.ACTIVE);
        }

        if (bounced) {
            setTargetDirection(state.getDirectionDeg());
        }
    }

    /**
     * Sets target heading used by turn-rate-limited rotation.
     *
     * @param direction desired heading in degrees
     */
    public void setTargetDirection(double direction) {
        long scaled = Math.round(normalizeDirection(direction) * DEGREE_SCALE);
        targetDirectionDegTimes100.set(scaled);
    }

    /**
     * @return target heading in degrees
     */
    public double getTargetDirection() {
        return targetDirectionDegTimes100.get() / DEGREE_SCALE;
    }

    /**
     * Computes shortest signed rotation angle from one heading to another.
     *
     * @param fromDeg starting heading in degrees
     * @param toDeg target heading in degrees
     * @return signed delta in range [-180, 180] degrees
     */
    private double shortestSignedDeltaDeg(double fromDeg, double toDeg) {
        double delta = (toDeg - fromDeg + (REVERSE_ANGLE_DEG + FULL_CIRCLE_DEG)) % FULL_CIRCLE_DEG - REVERSE_ANGLE_DEG;
        if (delta == SIGNED_DELTA_MIN) {
            return REVERSE_ANGLE_DEG;
        }
        return delta;
    }

    /**
     * Normalizes direction to range [0, 360) degrees.
     *
     * @param direction input angle in degrees
     * @return normalized direction
     */
    private double normalizeDirection(double direction) {
        double normalized = direction % FULL_CIRCLE_DEG;
        return normalized < 0.0 ? normalized + FULL_CIRCLE_DEG : normalized;
    }
}

