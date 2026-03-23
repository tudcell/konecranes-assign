package com.example.konecranes.vehicle;

import com.example.konecranes.ai.AvoidanceDecisionEngine;
import com.example.konecranes.ai.RiskEstimator;
import com.example.konecranes.messaging.ControlCommand;
import com.example.konecranes.model.AvoidanceAction;
import com.example.konecranes.model.VehicleState;
import com.example.konecranes.model.VehicleStatus;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * Handles manual override timing and AI action selection.
 */
public class VehicleControlPolicy {

    private static final long MANUAL_OVERRIDE_HOLD_MILLIS = 2000L;

    private final VehicleProcessConfig config;
    private final AvoidanceDecisionEngine decisionEngine = new AvoidanceDecisionEngine(new RiskEstimator(20, 0.1));
    private final AtomicBoolean manualOverrideActive = new AtomicBoolean(false);
    private final AtomicLong manualOverrideUntilMillis = new AtomicLong(0L);

    public VehicleControlPolicy(VehicleProcessConfig config) {
        this.config = config;
    }

    public void applyControlCommand(ControlCommand command, VehicleState state, DoubleConsumer targetDirectionSetter) {
        if (command.getOverrideDirectionDeg() != null) {
            targetDirectionSetter.accept(command.getOverrideDirectionDeg());
        }
        if (command.getOverrideSpeed() != null) {
            state.setSpeed(Math.max(0.0, command.getOverrideSpeed()));
        }
        if (command.isManualOverride()) {
            manualOverrideActive.set(true);
            manualOverrideUntilMillis.set(System.currentTimeMillis() + MANUAL_OVERRIDE_HOLD_MILLIS);
            state.setCurrentAction(AvoidanceAction.USER_OVERRIDE);
        }
    }

    public void aiTick(VehicleState current,
                       List<VehicleState> context,
                       DoubleSupplier targetDirectionGetter,
                       DoubleConsumer targetDirectionSetter) {
        if (manualOverrideActive.get()) {
            if (System.currentTimeMillis() < manualOverrideUntilMillis.get()) {
                return;
            }
            manualOverrideActive.set(false);
        }

        AvoidanceDecisionEngine.DecisionResult result = decisionEngine.choose(current.copy(), context);

        current.setCurrentRiskScore(result.getRiskScore());
        current.setRiskLevel(result.getRiskLevel());
        current.setCurrentAction(result.getAction());

        switch (result.getAction()) {
            case TURN_LEFT:
                targetDirectionSetter.accept(targetDirectionGetter.getAsDouble() - 20.0);
                break;
            case TURN_RIGHT:
                targetDirectionSetter.accept(targetDirectionGetter.getAsDouble() + 20.0);
                break;
            case SLOW_DOWN:
                current.setSpeed(Math.max(15.0, current.getSpeed() * 0.85));
                break;
            case EMERGENCY_STOP:
                current.setSpeed(0.0);
                current.setStatus(VehicleStatus.STOPPED);
                break;
            case KEEP_COURSE:
                if (current.getStatus() == VehicleStatus.STOPPED) {
                    current.setSpeed(config.getInitialSpeed());
                }
                current.setStatus(VehicleStatus.ACTIVE);
                break;
            default:
                break;
        }
    }
}

