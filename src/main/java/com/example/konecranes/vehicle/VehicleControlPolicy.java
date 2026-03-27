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
 * Controls manual override timing and AI-based maneuver selection.
 *
 * This policy decides whether the vehicle should:
 * - honor a temporary manual override
 * - keep course
 * - turn
 * - slow down
 * - stop
 */
public class VehicleControlPolicy {

    private final VehicleProcessConfig config;
    private final AvoidanceDecisionEngine decisionEngine;
    private final AtomicBoolean manualOverrideActive = new AtomicBoolean(false);
    private final AtomicLong manualOverrideUntilMillis = new AtomicLong(0L);

    public VehicleControlPolicy(VehicleProcessConfig config) {
        this.config = config;
        this.decisionEngine = new AvoidanceDecisionEngine(
                new RiskEstimator(config.getAiPredictionSteps(), config.getAiPredictionStepSeconds()),
                config.getAiKeepCourseRiskThreshold()
        );
    }

    /**
     * Applies one manual control command.
     *
     * This may:
     * - update target direction
     * - update speed
     * - activate temporary manual override mode
     *
     * @param command command received from the coordinator
     * @param state mutable current vehicle state
     * @param targetDirectionSetter callback used to update steering target
     */
    public void applyControlCommand(ControlCommand command,
                                    VehicleState state,
                                    DoubleConsumer targetDirectionSetter) {
        if (command == null) {
            return;
        }

        if (command.getOverrideDirectionDeg() != null) {
            targetDirectionSetter.accept(command.getOverrideDirectionDeg());
        }

        if (command.getOverrideSpeed() != null) {
            state.setSpeed(Math.max(0.0, command.getOverrideSpeed()));
        }

        if (command.isManualOverride()) {
            manualOverrideActive.set(true);
            manualOverrideUntilMillis.set(System.currentTimeMillis() + config.getManualOverrideHoldMillis());
            state.setCurrentAction(AvoidanceAction.USER_OVERRIDE);
        }
    }

    /**
     * Executes one AI control tick when manual override is not active.
     *
     * The decision engine evaluates the current vehicle state
     * and nearby context, then updates steering, speed, status,
     * and risk fields accordingly.
     *
     * @param current mutable current vehicle state
     * @param context nearby vehicle states
     * @param targetDirectionGetter callback used to read current target heading
     * @param targetDirectionSetter callback used to update target heading
     */
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
                targetDirectionSetter.accept(
                        targetDirectionGetter.getAsDouble() - config.getAiTurnDeltaDeg()
                );
                break;

            case TURN_RIGHT:
                targetDirectionSetter.accept(
                        targetDirectionGetter.getAsDouble() + config.getAiTurnDeltaDeg()
                );
                break;

            case SLOW_DOWN:
                // Prevent repeated slowdown from reducing speed indefinitely.
                if (current.getSpeed() > config.getInitialSpeed()) {
                    current.setSpeed(Math.max(
                            config.getInitialSpeed(),
                            current.getSpeed() * config.getAiSlowDownFactor()
                    ));
                }
                break;

            case EMERGENCY_STOP:
                current.setSpeed(0.0);
                current.setStatus(VehicleStatus.STOPPED);
                break;

            case KEEP_COURSE:
                if (current.getStatus() == VehicleStatus.STOPPED) {
                    current.setSpeed(config.getInitialSpeed());
                } else if (current.getSpeed() < config.getInitialSpeed()) {
                    // Restore speed gradually when the vehicle is safe again.
                    current.setSpeed(Math.min(
                            config.getInitialSpeed(),
                            current.getSpeed() * config.getAiRecoveryFactor()
                    ));
                }
                current.setStatus(VehicleStatus.ACTIVE);
                break;

            default:
                break;
        }
    }
}