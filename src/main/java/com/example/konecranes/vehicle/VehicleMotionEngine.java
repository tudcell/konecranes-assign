package com.example.konecranes.vehicle;

import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles heading changes and boundary bounce behavior for one vehicle.
 *
 * Responsible for:
 * - rotating the vehicle toward a target direction
 * - reflecting movement at world boundaries
 * - restoring motion after a bounce when needed
 */
public class VehicleMotionEngine {

    private static final double REVERSE_ANGLE_DEG = 180.0;
    private static final double FULL_CIRCLE_DEG = 360.0;
    private static final double DEGREE_SCALE = 100.0;
    private static final double SIGNED_DELTA_MIN = -180.0;

    private final VehicleProcessConfig vehicleProcessConfig;
    private final AtomicLong targetDirectionDegTimes100 = new AtomicLong(0L);

    public VehicleMotionEngine(VehicleProcessConfig vehicleProcessConfig, double initialDirectionDeg) {
        this.vehicleProcessConfig = vehicleProcessConfig;
        setTargetDirection(initialDirectionDeg);
    }

    /**
     * Rotates the current heading toward the target heading.
     *
     * The applied turn is limited by the configured maximum
     * turn rate per tick.
     *
     * @param vehicleState mutable vehicle state
     */
    public void rotateTowardsTarget(VehicleState vehicleState) {
        double current = normalizeDirection(vehicleState.getDirectionDeg());
        double target = getTargetDirection();
        double delta = shortestSignedDeltaDeg(current, target);
        double boundedDelta = Math.max(
                -vehicleProcessConfig.getMaxTurnDegPerTick(),
                Math.min(vehicleProcessConfig.getMaxTurnDegPerTick(), delta)
        );

        vehicleState.setDirectionDeg(normalizeDirection(current + boundedDelta));
    }

    /**
     * Applies boundary bounce behavior when the vehicle reaches world edges.
     *
     * If a bounce happens:
     * - position is clamped to the boundary
     * - heading is reflected
     * - target direction is updated to match the new heading
     * - stopped vehicles may resume motion
     *
     * @param vehicleState mutable vehicle state
     */
    public void bounceIfNeeded(VehicleState vehicleState) {
        double radius = vehicleState.getRadius();
        boolean bounced = false;

        if (vehicleState.getX() <= radius) {
            vehicleState.setX(radius);
            vehicleState.setDirectionDeg(normalizeDirection(REVERSE_ANGLE_DEG - vehicleState.getDirectionDeg()));
            bounced = true;
        } else if (vehicleState.getX() >= vehicleProcessConfig.getWorldWidth() - radius) {
            vehicleState.setX(vehicleProcessConfig.getWorldWidth() - radius);
            vehicleState.setDirectionDeg(normalizeDirection(REVERSE_ANGLE_DEG - vehicleState.getDirectionDeg()));
            bounced = true;
        }

        if (vehicleState.getY() <= radius) {
            vehicleState.setY(radius);
            vehicleState.setDirectionDeg(normalizeDirection(FULL_CIRCLE_DEG - vehicleState.getDirectionDeg()));
            bounced = true;
        } else if (vehicleState.getY() >= vehicleProcessConfig.getWorldHeight() - radius) {
            vehicleState.setY(vehicleProcessConfig.getWorldHeight() - radius);
            vehicleState.setDirectionDeg(normalizeDirection(FULL_CIRCLE_DEG - vehicleState.getDirectionDeg()));
            bounced = true;
        }

        if (bounced && vehicleState.getSpeed() <= 0.0) {
            vehicleState.setSpeed(vehicleProcessConfig.getInitialSpeed());
            vehicleState.setStatus(VehicleStatus.ACTIVE);
        }

        if (bounced) {
            setTargetDirection(vehicleState.getDirectionDeg());
        }
    }

    /**
     * Updates the target heading used by the motion engine.
     *
     * The value is stored as a scaled integer to keep updates atomic
     * while preserving two decimal places of precision.
     *
     * @param direction desired heading in degrees
     */
    public void setTargetDirection(double direction) {
        long scaled = Math.round(normalizeDirection(direction) * DEGREE_SCALE);
        targetDirectionDegTimes100.set(scaled);
    }

    /**
     * Returns the current target heading.
     *
     * @return target direction in degrees
     */
    public double getTargetDirection() {
        return targetDirectionDegTimes100.get() / DEGREE_SCALE;
    }

    /**
     * Computes the shortest signed angular delta between two headings.
     *
     * The result is normalized to the range [-180, 180].
     *
     * @param fromDeg starting heading in degrees
     * @param toDeg target heading in degrees
     * @return signed angular delta
     */
    private double shortestSignedDeltaDeg(double fromDeg, double toDeg) {
        double delta =
                (toDeg - fromDeg + (REVERSE_ANGLE_DEG + FULL_CIRCLE_DEG))
                        % FULL_CIRCLE_DEG
                        - REVERSE_ANGLE_DEG;

        if (delta == SIGNED_DELTA_MIN) {
            return REVERSE_ANGLE_DEG;
        }

        return delta;
    }

    /**
     * Normalizes a direction into the range [0, 360).
     *
     * @param direction input angle in degrees
     * @return normalized direction
     */
    private double normalizeDirection(double direction) {
        double normalized = direction % FULL_CIRCLE_DEG;
        return normalized < 0.0 ? normalized + FULL_CIRCLE_DEG : normalized;
    }
}