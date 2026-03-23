package com.example.konecranes.vehicle;

import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles heading and boundary movement behavior.
 */
public class VehicleMotionEngine {

    private static final double MAX_TURN_DEG_PER_TICK = 12.0;

    private final VehicleProcessConfig config;
    private final AtomicLong targetDirectionDegTimes100 = new AtomicLong(0L);

    public VehicleMotionEngine(VehicleProcessConfig config, double initialDirectionDeg) {
        this.config = config;
        setTargetDirection(initialDirectionDeg);
    }

    public void rotateTowardsTarget(VehicleState state) {
        double current = normalizeDirection(state.getDirectionDeg());
        double target = getTargetDirection();
        double delta = shortestSignedDeltaDeg(current, target);
        double boundedDelta = Math.max(-MAX_TURN_DEG_PER_TICK, Math.min(MAX_TURN_DEG_PER_TICK, delta));
        state.setDirectionDeg(normalizeDirection(current + boundedDelta));
    }

    public void bounceIfNeeded(VehicleState state) {
        double radius = state.getRadius();
        boolean bounced = false;

        if (state.getX() <= radius) {
            state.setX(radius);
            state.setDirectionDeg(normalizeDirection(180.0 - state.getDirectionDeg()));
            bounced = true;
        } else if (state.getX() >= config.getWorldWidth() - radius) {
            state.setX(config.getWorldWidth() - radius);
            state.setDirectionDeg(normalizeDirection(180.0 - state.getDirectionDeg()));
            bounced = true;
        }

        if (state.getY() <= radius) {
            state.setY(radius);
            state.setDirectionDeg(normalizeDirection(360.0 - state.getDirectionDeg()));
            bounced = true;
        } else if (state.getY() >= config.getWorldHeight() - radius) {
            state.setY(config.getWorldHeight() - radius);
            state.setDirectionDeg(normalizeDirection(360.0 - state.getDirectionDeg()));
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

    public void setTargetDirection(double direction) {
        long scaled = Math.round(normalizeDirection(direction) * 100.0);
        targetDirectionDegTimes100.set(scaled);
    }

    public double getTargetDirection() {
        return targetDirectionDegTimes100.get() / 100.0;
    }

    private double shortestSignedDeltaDeg(double fromDeg, double toDeg) {
        double delta = (toDeg - fromDeg + 540.0) % 360.0 - 180.0;
        if (delta == -180.0) {
            return 180.0;
        }
        return delta;
    }

    private double normalizeDirection(double direction) {
        double normalized = direction % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }
}

