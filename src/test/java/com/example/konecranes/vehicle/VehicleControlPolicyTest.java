package com.example.konecranes.vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class VehicleControlPolicyTest {
    @Test
    void canInstantiateAndApplyControlCommand() {
        VehicleProcessConfig config = VehicleProcessConfig.fromArgs(new String[]{"--vehicleId=VH-UNITTEST"});
        VehicleControlPolicy policy = new VehicleControlPolicy(config);
        assertNotNull(policy);
        // Test applyControlCommand
        com.example.konecranes.messaging.ControlCommand command = new com.example.konecranes.messaging.ControlCommand();
        command.setManualOverride(true);
        com.example.konecranes.model.VehicleState state = new com.example.konecranes.model.VehicleState();
        java.util.concurrent.atomic.AtomicReference<Double> targetDirection = new java.util.concurrent.atomic.AtomicReference<>(0.0);
        policy.applyControlCommand(command, state, targetDirection::set);
        assertEquals(com.example.konecranes.model.AvoidanceAction.USER_OVERRIDE, state.getCurrentAction());
    }
}
